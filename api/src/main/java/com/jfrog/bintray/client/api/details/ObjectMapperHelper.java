package com.jfrog.bintray.client.api.details;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 * Helper class that provides the ObjectMapper for all Details classes
 *
 * @author Dan Feldman
 */
public class ObjectMapperHelper {

    public static final ObjectMapper objectMapper = buildDetailsMapper();

    private static ObjectMapper buildDetailsMapper() {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());

        // TODO: when moving to Jackson 2.x these can be replaced with JodaModule
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        return mapper;
    }
}
