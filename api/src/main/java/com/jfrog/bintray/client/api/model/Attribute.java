package com.jfrog.bintray.client.api.model;

import java.util.List;

/**
 * @author jbaruch
 * @since 10/14/14
 */
public interface Attribute<T> {
    enum Type {
        STRING, DATE, NUMBER, BOOLEAN
    }

    String name();
    List<T> values();
    Type type();
}
