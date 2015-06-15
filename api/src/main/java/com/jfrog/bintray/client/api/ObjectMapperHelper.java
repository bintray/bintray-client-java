package com.jfrog.bintray.client.api;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Helper class that provides the ObjectMapper for all Details classes
 *
 * @author Dan Feldman
 */
public class ObjectMapperHelper {

    public static ObjectMapper get() {
        return buildDetailsMapper();
    }

    private static ObjectMapper buildDetailsMapper() {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());

        // TODO: when moving to Jackson 2.x these can be replaced with JodaModule
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

        //Don't include keys with null values implicitly, only explicitly set values should be sent over
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false);
        mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        return mapper;
    }
}
