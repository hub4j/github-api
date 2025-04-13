package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * A Github Marketplace purchase pending change.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceListAccountBuilder#createRequest()
 */
public class GHMarketplacePendingChange extends GitHubInteractiveObject {

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private String effectiveDate;

    private long id;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePlan plan;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private Long unitCount;
    /**
     * Create default GHMarketplacePendingChange instance
     */
    public GHMarketplacePendingChange() {
    }

    /**
     * Gets effective date.
     *
     * @return the effective date
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getEffectiveDate() {
        return GitHubClient.parseInstant(effectiveDate);
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
     * Gets plan.
     *
     * @return the plan
     */
    public GHMarketplacePlan getPlan() {
        return plan;
    }

    /**
     * Gets unit count.
     *
     * @return the unit count
     */
    public Long getUnitCount() {
        return unitCount;
    }

}
