package de.sliceanddice.maryandpaul.lib.requests;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import de.sliceanddice.maryandpaul.lib.enums.FacetGroup;

public class FacetRequest extends BaseRequest {

    private Facet facets = new Facet();

    private static class Facet {
        @SerializedName("group_ids")
        private List<FacetGroup> facetGroups;
        private Integer limit;
        private Integer offset;
    }

    public static class Builder {

        private List<FacetGroup> facetGroups;
        private Integer limit;
        private Integer offset;

        public Builder filterByFacetGroup(List<FacetGroup> facetGroups) {
            this.facetGroups = facetGroups;
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

        public FacetRequest build() {
            FacetRequest facetRequest = new FacetRequest();

            Facet facet = new Facet();
            facet.facetGroups = facetGroups;
            facet.limit = limit;
            facet.offset = offset;
            facetRequest.facets = facet;

            return facetRequest;
        }

    }
}