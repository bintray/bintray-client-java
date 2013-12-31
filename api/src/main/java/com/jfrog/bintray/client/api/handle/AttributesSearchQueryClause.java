package com.jfrog.bintray.client.api.handle;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author jbaruch
 * @since 13/11/13
 */
public interface AttributesSearchQueryClause<T> {

    AttributesSearchQueryClause in(String... values);

    AttributesSearchQueryClause equals(String value);

    AttributesSearchQueryClause greaterThan(int value);

    AttributesSearchQueryClause greaterOrEqualsTo(int value);

    AttributesSearchQueryClause equals(int value);

    AttributesSearchQueryClause lessThan(int value);

    AttributesSearchQueryClause lessOrEquals(int value);

    AttributesSearchQueryClause before(DateTime value);

    AttributesSearchQueryClause beforOrAt(DateTime value);

    AttributesSearchQueryClause at(DateTime value);

    AttributesSearchQueryClause after(DateTime value);

    AttributesSearchQueryClause afterOrAt(DateTime value);

    ArtibutesSearchQuery and();
    List<T> search();
}
