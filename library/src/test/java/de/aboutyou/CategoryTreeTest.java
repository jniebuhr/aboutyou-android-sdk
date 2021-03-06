package de.aboutyou;

import org.junit.Test;

import de.aboutyou.models.CategoryTree;
import de.aboutyou.util.MockClient;

import static org.junit.Assert.*;

public class CategoryTreeTest extends TestBase {

    @Test
    public void testValidRequest() {
        ShopApiClient shopApiClient = getNewApiClient(new ValidRequestMockClient());

        shopApiClient.requestCategoryTree();
    }

    private class ValidRequestMockClient extends MockClient {

        @Override
        protected void validateRequestBody(String requestBody) {
            assertEquals("[{\"category_tree\":{}}]", requestBody);
        }

        @Override
        protected String getResponse() {
            return "[{\"category_tree\":[]}]";
        }

    }

    @Test
    public void testValidResponse() {
        ShopApiClient shopApiClient = getNewApiClient(new ValidResponseMockClient());

        CategoryTree categoryTree = shopApiClient.requestCategoryTree();

        assertNotNull(categoryTree);
        assertTrue(categoryTree.getAllCategories().size() == 2);
        assertTrue(categoryTree.getActiveCategories().size() == 1);
        assertEquals("Damen", categoryTree.getAllCategories().get(0).getName());
    }

    private class ValidResponseMockClient extends MockClient {

        @Override
        protected String getResponse() {
            return "[{\"category_tree\":[{\"name\":\"Damen\",\"parent\":null,\"sub_categories\":[],\"active\":true,\"position\":1,\"id\":1},{\"name\":\"Herren\",\"parent\":null,\"sub_categories\":[],\"active\":false,\"position\":2,\"id\":2}]}]";
        }

    }
}