package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.model.Pkg;
import com.jfrog.bintray.client.api.model.Version;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * @author jbaruch
 * @since 13/11/13
 */
public interface AttributesSearchQueryClause {

    AttributesSearchQueryClause in(String... values);

    AttributesSearchQueryClause equalsVal(Object value);

    AttributesSearchQueryClause greaterThan(int value);

    AttributesSearchQueryClause greaterOrEqualsTo(int value);

    AttributesSearchQueryClause lessThan(int value);

    AttributesSearchQueryClause lessOrEquals(int value);

    AttributesSearchQueryClause before(DateTime value);

    AttributesSearchQueryClause beforeOrAt(DateTime value);

    AttributesSearchQueryClause at(DateTime value);

    AttributesSearchQueryClause after(DateTime value);

    AttributesSearchQueryClause afterOrAt(DateTime value);

    AttributesSearchQuery and();

    List<Pkg> searchPackage() throws IOException;

    List<Version> searchVersion() throws IOException;
}
