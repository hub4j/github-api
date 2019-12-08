package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GHMarketplacePriceModel {
    FREE("free"), PER_UNIT("per-unit"), FLAT_RATE("flat-rate");

    @JsonValue
    private final String internalName;

    GHMarketplacePriceModel(String internalName) {
        this.internalName = internalName;
    }

    /**
     * Returns GitHub's internal representation of this event.
     */
    public String symbol() {
        return internalName;
    }
}
