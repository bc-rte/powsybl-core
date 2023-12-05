package com.powsybl.security.detectors.criterion.network;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNetworkElementCriterion {

    enum NetworkElementCriterionType {
        LINE,
        TWO_WINDING_TRANSFORMER,
        THREE_WINDING_TRANSFORMER
    }

    private Set<String> networkElementIds = new HashSet<>();

    protected AbstractNetworkElementCriterion(Set<String> networkElementIds) {
        this.networkElementIds = networkElementIds;
    }

    public Set<String> getNetworkElementIds() {
        return networkElementIds;
    }

    protected abstract NetworkElementCriterionType getNetworkElementCriterionType();

    public abstract boolean accept(NetworkElementVisitor networkElementVisitor);

}
