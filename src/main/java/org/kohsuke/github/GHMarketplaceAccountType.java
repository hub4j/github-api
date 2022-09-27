package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * GitHub Marketplace Account type.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceAccount
 */
public enum GHMarketplaceAccountType {
    
    /** The organization. */
    ORGANIZATION, 
 /** The user. */
 USER;

    /**
     * Returns GitHub's internal representation of this event.
     *
     * @return the string
     */
    String symbol() {
        return StringUtils.capitalize(name().toLowerCase(Locale.ENGLISH));
    }
}
