package de.aboutyou.models;

import java.util.HashMap;
import java.util.List;

import de.aboutyou.enums.FacetGroup;

public class Attributes extends HashMap<Integer, List<Long>> {

    public List<Long> get(FacetGroup facetGroup) {
        if (contains(facetGroup)){
            return get(facetGroup.getId());
        } else {
            return null;
        }
    }

    public boolean contains(FacetGroup facetGroup) {
        return containsKey(facetGroup.getId());
    }

}