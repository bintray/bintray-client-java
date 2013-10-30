package com.jfrog.bintray.client.api.builder;

import org.joda.time.DateTime;

/**
 * @author Noam Y. Tenne
 */
public class VersionBuilder {

    String description = null;
    DateTime released = null;

    public VersionBuilder description(String description) {
        this.description = description;
        return this;
    }

    public VersionBuilder released(DateTime released) {
        this.released = released;
        return this;
    }
}
