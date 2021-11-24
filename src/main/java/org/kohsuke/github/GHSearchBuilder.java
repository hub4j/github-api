package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for various search builders.
 *
 * @param <T>
 *            the type parameter
 * @author Kohsuke Kawaguchi
 */
public abstract class GHSearchBuilder<T> extends GHQueryBuilder<T> {
    protected final List<String> terms = new ArrayList<String>();

    /**
     * Data transfer object that receives the result of search.
     */
    private final Class<? extends SearchResult<T>> receiverType;

    GHSearchBuilder(GitHub root, Class<? extends SearchResult<T>> receiverType) {
        super(root);
        this.receiverType = receiverType;
        req.withUrlPath(getApiUrl());
        req.rateLimit(RateLimitTarget.SEARCH);
    }

    /**
     * Search terms.
     *
     * @param term
     *            the term
     * @return the gh query builder
     */
    public GHQueryBuilder<T> q(String term) {
        terms.add(term);
        return this;
    }

    /**
     * Add a search term with qualifier.
     *
     * If {@code value} is empty or {@code null}, all terms with the current qualifier will be removed.
     *
     * @param qualifier
     *            the qualifier for this term
     * @param value
     *            the value for this term. If empty or null, all terms with the current qualifier will be removed.
     * @return the gh query builder
     */
    GHQueryBuilder<T> q(@Nonnull final String qualifier, @CheckForNull final String value) {
        if (StringUtils.isEmpty(qualifier)) {
            throw new IllegalArgumentException("qualifier cannot be null or empty");
        }
        if (StringUtils.isEmpty(value)) {
            final String removeQualifier = qualifier + ":";
            terms.removeIf(term -> term.startsWith(removeQualifier));
        } else {
            terms.add(qualifier + ":" + value);
        }
        return this;
    }

    /**
     * Performs the search.
     */
    @Override
    public PagedSearchIterable<T> list() {

        req.set("q", StringUtils.join(terms, " "));
        return new PagedSearchIterable<>(root(), req.build(), receiverType);
    }

    /**
     * Gets api url.
     *
     * @return the api url
     */
    protected abstract String getApiUrl();
}
