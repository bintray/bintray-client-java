package com.jfrog.bintray.client.api.details;


import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * This class represents an attribute (version or package)
 * NOTE: when serializing this class use getObjectMapper to obtain a suitable mapper for this class
 *
 * @author Dan Feldman
 */
@JsonPropertyOrder({"name", "values", "type"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attribute<T> {
    private static final Logger log = LoggerFactory.getLogger(Attribute.class);
    @JsonProperty("name")
    private String name;
    @JsonProperty("values")
    private List<T> values;
    @JsonProperty("type")
    private Type type;

    @SafeVarargs
    public Attribute(String name, T... values) {
        this(name, null, asList(values));
    }


    @SafeVarargs
    public Attribute(String name, Type type, T... values) {
        this(name, type, asList(values));
    }

    @JsonCreator
    public Attribute(@JsonProperty("name") String name, @JsonProperty("type") Type type, @JsonProperty("values") List<T> values) {
        this.name = name;
        if (type == null) {
            type = Type.string;     //Type defaults to string
        }
        this.type = type;
        this.values = values;
    }

    public static ObjectMapper getObjectMapper() {
        return ObjectMapperHelper.objectMapper;
    }

    /**
     * Produces a json from a list of attributes
     *
     * @param attributeDetails List of attributes to serialize
     * @return A string representing the json
     * @throws IOException
     */
    @JsonIgnore
    public static String getJsonFromAttributeList(List<Attribute> attributeDetails) throws IOException {
        ObjectMapper mapper = ObjectMapperHelper.objectMapper;
        String jsonContent;
        try {
            jsonContent = mapper.writeValueAsString(attributeDetails);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            throw e;
        }
        return jsonContent;
    }

    @JsonIgnore
    public static List<Attribute> getAttributeListFromJson(InputStream inputStream) throws IOException {
        ObjectMapper mapper = ObjectMapperHelper.objectMapper;
        List<Attribute> attributes;
        try {
            attributes = mapper.readValue(inputStream, new TypeReference<List<Attribute>>() {
            });
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            throw e;
        }
        return attributes;
    }

    public String getName() {
        return name;
    }

    public List<T> getValues() {
        return values;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attribute attribute = (Attribute) o;

        if (!name.equals(attribute.name)) return false;
        if (!values.equals(attribute.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "Name='" + name + '\'' +
                ", Type=" + type +
                ", Values=" + values +
                '}';
    }

    public enum Type {
        string, date, number, Boolean
    }
}

