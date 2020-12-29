package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

/**
 * A Github Marketplace purchase pending change.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceListAccountBuilder#createRequest()
 */
public class GHMarketplacePendingChange extends GitHubInteractiveObject {
    private long id;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private Long unitCount;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePlan plan;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private String effectiveDate;

    /**
     * Wrap up gh marketplace pending change.
     *
     * @param root
     *            the root
     * @return an instance of the GHMarketplacePendingChange class
     */
    GHMarketplacePendingChange wrapUp(GitHub root) {
        this.root = root;
        if (plan != null) { // sanity check
            this.plan.wrapUp(this.root);
        }
        return this;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets unit count.
     *
     * @return the unit count
     */
    public Long getUnitCount() {
        return unitCount;
    }

    /**
     * Gets plan.
     *
     * @return the plan
     */
    public GHMarketplacePlan getPlan() {
        return plan;
    }

    /**
     * Gets effective date.
     *
     * @return the effective date
     */
    public Date getEffectiveDate() {
        return GitHubClient.parseDate(effectiveDate);
    }

}
