package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.handle.AttributesSearchQuery;
import com.jfrog.bintray.client.api.handle.AttributesSearchQueryClause;
import com.jfrog.bintray.client.api.model.Pkg;
import com.jfrog.bintray.client.api.model.Version;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jbaruch
 * @author Dan Feldman
 * @since 13/11/13
 */
@JsonSerialize(using = AttributesSearchQueryImpl.AttributeSearchQuerySerializer.class)
class AttributesSearchQueryImpl implements AttributesSearchQuery {

    private final RepositoryHandleImpl repositoryHandle;
    private final PackageHandleImpl packageHandle;
    private String attributeName;
    private List<AttributesSearchQueryClauseImpl> queryClauses = new ArrayList<>();

    /**
     * Creates an AttributesSearchQuery object that will search for a version
     *
     * @param repositoryHandle repository to search in
     */
    public AttributesSearchQueryImpl(RepositoryHandleImpl repositoryHandle) {
        this.repositoryHandle = repositoryHandle;
        this.packageHandle = null;
    }

    /**
     * Creates an AttributesSearchQuery object that will search for a version
     *
     * @param packageHandle version to search in
     */
    public AttributesSearchQueryImpl(PackageHandleImpl packageHandle) {
        this.packageHandle = packageHandle;
        this.repositoryHandle = null;
    }

    public void addQueryClause(AttributesSearchQueryClauseImpl clause) {
        queryClauses.add(clause);
    }

    public List<AttributesSearchQueryClauseImpl> getQueryClauses() {
        return queryClauses;
    }

    @Override
    public AttributesSearchQueryClause byAttributeName(String attributeName) {
        this.attributeName = attributeName;
        return new AttributesSearchQueryClauseImpl(this);
    }

    public List<Pkg> searchPackage() throws IOException, BintrayCallException {
        repositoryHandle.addQuery(this);
        return repositoryHandle.attributeSearch();
    }

    public List<Version> searchVersion() throws IOException, BintrayCallException {
        packageHandle.addQuery(this);
        return packageHandle.attributeSearch();
    }

    public static class AttributeSearchQuerySerializer extends JsonSerializer<AttributesSearchQueryImpl> {

        // TODO: add support for other search methods (greater, less than etc.) with Bintray's search syntax
        @Override
        public void serialize(AttributesSearchQueryImpl value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {

            jgen.writeStartArray();
            jgen.writeStartObject();
            jgen.writeArrayFieldStart(value.attributeName);

            @SuppressWarnings("unchecked")
            List<AttributesSearchQueryClauseImpl> clauses = value.getQueryClauses();
            for (AttributesSearchQueryClauseImpl clause : clauses) {
                if (clause.getType().equals(Attribute.Type.Boolean)) {
                    jgen.writeBoolean((Boolean) clause.getClauseValue());
                } else if (clause.getType().equals(Attribute.Type.number)) {
                    jgen.writeNumber(String.valueOf(clause.getClauseValue()));
                } else {  //String or Date
                    jgen.writeString((String) clause.getClauseValue());
                }
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
            jgen.writeEndArray();
        }
    }
}
