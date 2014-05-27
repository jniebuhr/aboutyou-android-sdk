package de.aboutyou.request;

import com.google.gson.annotations.SerializedName;

public class BasketGetRequest extends CollinsRequest {

    private Basket basket;

    private static class Basket {
        @SerializedName("session_id")
        private String sessionId;
    }

    public static class Builder extends CollinsRequest.Builder {

        private String sessionId;

        public Builder(String sessionId) {
            validateNotEmpty(sessionId, "sessionId");
            this.sessionId = sessionId;
        }

        public BasketGetRequest build() {
            BasketGetRequest basketAddRequest = new BasketGetRequest();

            Basket basket = new Basket();
            basket.sessionId = sessionId;
            basketAddRequest.basket = basket;

            return basketAddRequest;
        }

    }

}
