package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.handle.AttributesSearchQuery;
import com.jfrog.bintray.client.api.handle.AttributesSearchQueryClause;
import com.jfrog.bintray.client.api.model.Pkg;
import com.jfrog.bintray.client.api.model.Version;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * @author jbaruch
 * @author Dan Feldman
 * @since 13/11/13
 */
class AttributesSearchQueryClauseImpl implements AttributesSearchQueryClause {

    @JsonIgnore
    private final AttributesSearchQueryImpl query;
    @JsonIgnore
    private Object clauseValue;
    @JsonIgnore
    private Attribute.Type type;


    AttributesSearchQueryClauseImpl(AttributesSearchQueryImpl query) {
        this.query = query;
    }

    public Object getClauseValue() {
        return clauseValue;
    }

    public Attribute.Type getType() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AttributesSearchQueryClause equalsVal(Object value) {
        Attribute attribute = new Attribute("TypeTest", value); //This validates proper value type
        type = attribute.getType();
        clauseValue = value;
        return this;
    }

    @Override
    public AttributesSearchQuery and() {
        query.addQueryClause(this);
        return query;
    }

    @Override
    public List<Pkg> searchPackage() throws IOException {
        query.addQueryClause(this);
        return query.searchPackage();
    }

    @Override
    public List<Version> searchVersion() throws IOException {
        query.addQueryClause(this);
        return query.searchVersion();
    }

    @Override
    public AttributesSearchQueryClause lessThan(int value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause lessOrEquals(int value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause before(DateTime value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause beforeOrAt(DateTime value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause at(DateTime value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause after(DateTime value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause afterOrAt(DateTime value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause greaterThan(int value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause greaterOrEqualsTo(int value) {
        //TODO implement
        return this;
    }

    @Override
    public AttributesSearchQueryClause in(String... values) {
        //TODO implement
        return this;
    }
}
