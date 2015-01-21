package com.jfrog.bintray.client.api.handle;

/**
 * @author jbaruch
 * @since 13/11/13
 */
public interface AttributesSearchQuery {
    AttributesSearchQueryClause byAttributeName(String name);
}
