package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * GitHub Marketplace Account type.
 *
 * @see GHMarketplaceAccount
 */
public enum GHMarketplaceAccountType {
    ORGANIZATION, USER;

    /**
     * Returns GitHub's internal representation of this event.
     */
    String symbol() {
        return StringUtils.capitalize(name().toLowerCase(Locale.ENGLISH));
    }
}
