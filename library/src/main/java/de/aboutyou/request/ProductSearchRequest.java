package de.aboutyou.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import de.aboutyou.enums.Direction;
import de.aboutyou.enums.FacetType;
import de.aboutyou.enums.ProductFilter;
import de.aboutyou.enums.Sortby;

public class ProductSearchRequest extends CollinsRequest {

    @SerializedName("product_search")
    private ProductSearch productSearch = new ProductSearch();

    private static class ProductSearch {
        @SerializedName("session_id")
        private String sessionId;
        private Filter filter;
        private Result result;

        private static class Filter {
            private List<Long> categories;
            // true = sale only, false = non-sale only, NULL = all
            private Boolean sale;
            @SerializedName("prices")
            private Prices priceRange;
            private String searchword;
            private Map<FacetType, List<Long>> facets;

            private static class Prices {
                private Long from;
                private Long to;

                public Prices(Long from, Long to) {
                    this.from = from;
                    this.to = to;
                }
            }
        }

        private static class Result {
            private Sort sort;
            @SerializedName("price")
            private Boolean showPriceInformation;
            @SerializedName("sale")
            private Boolean showSaleInformation;
            private Integer limit;
            private Integer offset;
            @SerializedName("categories")
            private Boolean showCategories;

            private static class Sort {
                private Sortby by;
                private Direction direction;

                public Sort(Sortby by, Direction direction) {
                    this.by = by;
                    this.direction = direction;
                }
            }
        }
    }

    public static class Builder extends CollinsRequest.Builder<ProductSearchRequest> {

        private String sessionId;
        private List<Long> categories;
        private ProductFilter productFilter;
        private Long priceFrom;
        private Long priceTo;
        private String searchString;
        private Map<FacetType, List<Long>> facets;
        private Boolean listSaleDetails;
        private Boolean listPriceDetails;
        private Boolean listCategories;
        private Integer limit;
        private Integer offset;
        private Sortby sortby;
        private Direction sortDirection;

        /**
         * Constructs a new Builder for a {@link de.aboutyou.request.ProductSearchRequest}
         * @param sessionId A session id provided by the caller, not null or empty
         */
        public Builder(String sessionId) {
            validateNotEmpty(sessionId, "sessionId");
            this.sessionId = sessionId;
        }

        /** Filter results by category ids */
        public Builder filterByCategories(List<Long> categories) {
            this.categories = categories;
            return this;
        }

        /** Filter results by sale status */
        public Builder filterByStatus(ProductFilter productFilter) {
            this.productFilter = productFilter;
            return this;
        }

        /** Filter results by minimum price in cents */
        public Builder filterByMinPrice(long priceInCents) {
            this.priceFrom = priceInCents;
            return this;
        }

        /** Filter results by maximum price in cents */
        public Builder filterByMaxPrice(long priceInCents) {
            this.priceTo = priceInCents;
            return this;
        }

        /** Filter results by a search string */
        public Builder filterBySearchString(String searchString) {
            this.searchString = searchString;
            return this;
        }

        /** Filter results by facet ids */
        public Builder filterByFacets(Map<FacetType, List<Long>> facets) {
            this.facets = facets;
            return this;
        }

        /** Request sale details in the response */
        public Builder listSaleDetails(boolean listSaleDetails) {
            this.listSaleDetails = listSaleDetails;
            return this;
        }

        /** Request price details in the response */
        public Builder listPriceDetails(boolean listPriceDetails) {
            this.listPriceDetails = listPriceDetails;
            return this;
        }

        /** Request to list category ids in the response */
        public Builder listCategoriesWithResults(boolean listCategories) {
            this.listCategories = listCategories;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public Builder sortBy(Sortby sortby, Direction direction) {
            this.sortby = sortby;
            this.sortDirection = direction;
            return this;
        }

        public ProductSearchRequest build() {
            ProductSearch productSearch = new ProductSearch();
            productSearch.sessionId = sessionId;

            ProductSearch.Filter filter = new ProductSearch.Filter();
            filter.categories = categories;
            filter.facets = facets;
            filter.priceRange = new ProductSearch.Filter.Prices(priceFrom, priceTo);
            filter.searchword = searchString;
            filter.sale = productFilter != null ? productFilter.getValue() : null;
            productSearch.filter = filter;

            ProductSearch.Result result = new ProductSearch.Result();
            result.sort = new ProductSearch.Result.Sort(sortby, sortDirection);
            result.limit = limit;
            result.offset = offset;
            result.showCategories = listCategories;
            result.showSaleInformation = listSaleDetails;
            result.showPriceInformation = listPriceDetails;
            productSearch.result = result;

            ProductSearchRequest productSearchRequest = new ProductSearchRequest();
            productSearchRequest.productSearch = productSearch;

            return productSearchRequest;
        }

    }
}
