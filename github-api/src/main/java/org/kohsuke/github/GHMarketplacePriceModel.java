package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonValue;

// TODO: Auto-generated Javadoc
/**
 * GitHub Marketplace plan pricing model.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplacePlan
 */
public enum GHMarketplacePriceModel {

    /** The free. */
    FREE("FREE"),
    /** The per unit. */
    PER_UNIT("PER_UNIT"),
    /** The flat rate. */
    FLAT_RATE("FLAT_RATE");

    @JsonValue
    private final String internalName;

    /**
     * Instantiates a new GH marketplace price model.
     *
     * @param internalName
     *            the internal name
     */
    GHMarketplacePriceModel(String internalName) {
        this.internalName = internalName;
    }

    /**
     * Returns GitHub's internal representation of this event.
     *
     * @return a string containing GitHub's internal representation of this event.
     */
    public String symbol() {
        return internalName;
    }
}
