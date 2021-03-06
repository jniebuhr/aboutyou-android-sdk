package de.aboutyou.internal.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import de.aboutyou.enums.AuthScope;
import de.aboutyou.enums.AuthenticationRequestMode;
import de.aboutyou.enums.Endpoint;
import de.aboutyou.internal.util.UrlUtil;
import de.aboutyou.R;

// Based on Facebook SDK WebDialog
public class AuthWebDialog extends Dialog {

    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_REDIRECT_URI = "redirect_uri";
    private static final String PARAM_RESPONSE_TYPE = "response_type";
    private static final String PARAM_SCOPE = "scope";
    private static final String PARAM_POPUP = "popup";
    private static final String PARAM_REGISTER = "register";

    private static final int NO_PADDING_SCREEN_WIDTH = 480;
    private static final int MAX_PADDING_SCREEN_WIDTH = 800;
    private static final int NO_PADDING_SCREEN_HEIGHT = 800;
    private static final int MAX_PADDING_SCREEN_HEIGHT = 1280;

    private static final double MIN_SCALE_FACTOR = 0.5;

    private static final int DEFAULT_THEME = android.R.style.Theme_Translucent_NoTitleBar;
    private static final int BACKGROUND_GRAY = 0xCC000000;

    private String mUrl;
    private String mShopUrl;
    private String mShopUrlMobile;
    private String mRedirectUrl;

    private FrameLayout mContentFrameLayout;
    private WebView mWebView;
    private ImageView mCrossImageView;
    private ProgressDialog mSpinner;

    private OnCompleteListener mOnCompleteListener;

    private boolean mListenerCalled = false;
    private boolean mIsDetached = false;

    public interface OnCompleteListener {

        public void onComplete(String accessToken);

        public void onAbort();

        public void onError();
    }

    public AuthWebDialog(Context context, String clientId, Endpoint endpoint, List<AuthScope> scopes, AuthenticationRequestMode mode, String redirectUrl, OnCompleteListener listener) {
        super(context, DEFAULT_THEME);

        Bundle parameters = new Bundle();

        parameters.putString(PARAM_CLIENT_ID, clientId);
        parameters.putString(PARAM_REDIRECT_URI, redirectUrl);
        parameters.putString(PARAM_RESPONSE_TYPE, "token");
        parameters.putString(PARAM_POPUP, "true");

        StringBuilder sb = new StringBuilder();
        for (AuthScope scope : scopes) {
            sb.append(scope.getScope());
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length()-1);

        parameters.putString(PARAM_SCOPE, sb.toString());

        if (mode == AuthenticationRequestMode.REGISTER) {
            parameters.putString(PARAM_REGISTER, "true");
        }

        mUrl = UrlUtil.buildUri("https", endpoint.getAuthAuthority(), null, parameters).toString();
        mShopUrl = UrlUtil.buildUri("https", endpoint.getAuthAuthority(), null, null).toString();
        mShopUrlMobile = UrlUtil.buildUri("https", endpoint.getAuthAuthorityMobile(), null, null).toString();
        mRedirectUrl = redirectUrl;

        mOnCompleteListener = listener;
    }

    @Override
    public void dismiss() {
        if (mWebView != null) {
            mWebView.stopLoading();
        }
        if (!mIsDetached) {
            if (mSpinner.isShowing()) {
                mSpinner.dismiss();
            }
            super.dismiss();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        mIsDetached = true;
        super.onDetachedFromWindow();
    }

    @Override
    public void onAttachedToWindow() {
        mIsDetached = false;
        super.onAttachedToWindow();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                sendCancelToListener();
            }
        });

        mSpinner = new ProgressDialog(getContext(), resolveDialogTheme());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading");
        mSpinner.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                sendCancelToListener();
                AuthWebDialog.this.dismiss();
            }
        });

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContentFrameLayout = new FrameLayout(getContext());

        calculateSize();
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        createCrossImage();
        int crossWidth = mCrossImageView.getDrawable().getIntrinsicWidth();

        setUpWebView(crossWidth / 2 + 1);

        mContentFrameLayout.addView(mCrossImageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(mContentFrameLayout);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int resolveDialogTheme() {
        int build = Build.VERSION.SDK_INT;
        if (build <= Build.VERSION_CODES.HONEYCOMB) {
            return 0;
        } else if (build <= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return AlertDialog.THEME_HOLO_LIGHT;
        } else {
            return AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
        }
    }

    private void calculateSize() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        // always use the portrait dimensions to do the scaling calculations so we always get a portrait shaped
        // web dialog
        int width = metrics.widthPixels < metrics.heightPixels ? metrics.widthPixels : metrics.heightPixels;
        int height = metrics.widthPixels < metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;

        int dialogWidth = Math.min(getScaledSize(width, metrics.density, NO_PADDING_SCREEN_WIDTH, MAX_PADDING_SCREEN_WIDTH), metrics.widthPixels);
        int dialogHeight = Math.min(getScaledSize(height, metrics.density, NO_PADDING_SCREEN_HEIGHT, MAX_PADDING_SCREEN_HEIGHT), metrics.heightPixels);

        getWindow().setLayout(dialogWidth, dialogHeight);
    }

    private int getScaledSize(int screenSize, float density, int noPaddingSize, int maxPaddingSize) {
        int scaledSize = (int) ((float) screenSize / density);
        double scaleFactor;
        if (scaledSize <= noPaddingSize) {
            scaleFactor = 1.0;
        } else if (scaledSize >= maxPaddingSize) {
            scaleFactor = MIN_SCALE_FACTOR;
        } else {
            // between the noPadding and maxPadding widths, we take a linear reduction to go from 100%
            // of screen size down to MIN_SCALE_FACTOR
            scaleFactor = MIN_SCALE_FACTOR + ((double) (maxPaddingSize - scaledSize)) / ((double) (maxPaddingSize - noPaddingSize)) * (1.0 - MIN_SCALE_FACTOR);
        }
        return (int) (screenSize * scaleFactor);
    }

    private void sendSuccessToListener(String accessToken) {
        if (mOnCompleteListener != null && !mListenerCalled) {
            mListenerCalled = true;
            mOnCompleteListener.onComplete(accessToken);
        }
    }

    private void sendErrorToListener() {
        if (mOnCompleteListener != null && !mListenerCalled) {
            mListenerCalled = true;
            mOnCompleteListener.onError();
        }
    }

    private void sendCancelToListener() {
        if (mOnCompleteListener != null && !mListenerCalled) {
            mListenerCalled = true;
            mOnCompleteListener.onAbort();
        }
    }

    private void createCrossImage() {
        mCrossImageView = new ImageView(getContext());
        mCrossImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCancelToListener();
                AuthWebDialog.this.dismiss();
            }
        });
        Drawable crossDrawable = getContext().getResources().getDrawable(R.drawable.ic_close);
        mCrossImageView.setImageDrawable(crossDrawable);
        mCrossImageView.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView(int margin) {
        LinearLayout webViewContainer = new LinearLayout(getContext());
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new DialogWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);

        webViewContainer.setPadding(margin, margin, margin, margin);
        webViewContainer.addView(mWebView);
        webViewContainer.setBackgroundColor(BACKGROUND_GRAY);
        mContentFrameLayout.addView(webViewContainer);
    }

    private class DialogWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(mShopUrl) || url.startsWith(mShopUrlMobile)) {
                return false;
            } else if (url.startsWith(mRedirectUrl)) {
                Bundle values = UrlUtil.parseUrl(url);

                // TODO error handling

                String accessToken = values.getString("access_token");
                if (accessToken != null) {
                    sendSuccessToListener(accessToken);
                } else {
                    sendErrorToListener();
                }

                AuthWebDialog.this.dismiss();
                return true;
            }
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            sendErrorToListener();
            AuthWebDialog.this.dismiss();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            sendErrorToListener();
            handler.cancel();
            AuthWebDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (!mIsDetached) {
                mSpinner.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!mIsDetached) {
                mSpinner.dismiss();
            }

            mContentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            mWebView.setVisibility(View.VISIBLE);
            mCrossImageView.setVisibility(View.VISIBLE);
        }
    }
}