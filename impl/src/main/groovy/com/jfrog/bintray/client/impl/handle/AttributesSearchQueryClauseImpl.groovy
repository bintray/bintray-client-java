package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.handle.ArtibutesSearchQuery
import com.jfrog.bintray.client.api.handle.AttributesSearchQueryClause
import org.joda.time.DateTime

/**
 *
 * @author jbaruch
 * @since 13/11/13
 */
class AttributesSearchQueryClauseImpl<T> implements AttributesSearchQueryClause {

    private final ArtibutesSearchQueryImpl query
    private def clauseValue

    AttributesSearchQueryClauseImpl(ArtibutesSearchQueryImpl query) {
        this.query = query
    }

    AttributesSearchQueryClause "in"(String... values) {
       //TODO implement
        this
    }
    AttributesSearchQueryClause equals(String value) {
        clauseValue = value
        this
    }

    AttributesSearchQueryClause greaterThan(int value) {
        //TODO implement
        this
    }

    AttributesSearchQueryClause greaterOrEqualsTo(int value) {
        //TODO implement
        this
    }

    AttributesSearchQueryClause equals(int value) {
        clauseValue = value
        this
    }

    AttributesSearchQueryClause lessThan(int value) {
        //TODO implement
        this
    }
    AttributesSearchQueryClause lessOrEquals(int value) {
        //TODO implement
        this
    }

    AttributesSearchQueryClause before(DateTime value) {
        //TODO implement
        this
    }
   AttributesSearchQueryClause beforOrAt(DateTime value) {
       //TODO implement
        this
    }
   AttributesSearchQueryClause at(DateTime value) {
       //TODO implement
        this
    }
   AttributesSearchQueryClause after(DateTime value) {
       //TODO implement
        this
    }
    AttributesSearchQueryClause afterOrAt(DateTime value) {
        //TODO implement
        this
    }

    @SuppressWarnings("GroovyAccessibility")
    ArtibutesSearchQuery and(){
        query.queryClauses << this
        return query
    }

    @Override
    @SuppressWarnings("GroovyAccessibility")
    List<T> search() {
        query.queryClauses << this
        return query.search()
    }
}
