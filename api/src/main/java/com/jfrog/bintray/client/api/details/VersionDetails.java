package com.jfrog.bintray.client.api.details;

import org.joda.time.DateTime;

/**
 * @author Noam Y. Tenne
 */
public class VersionDetails {

    String description = null;
    DateTime released = null;
    String name;

    public VersionDetails(String name) {
        this.name = name;
    }

    public VersionDetails description(String description) {
        this.description = description;
        return this;
    }

    public VersionDetails released(DateTime released) {
        this.released = released;
        return this;
    }
}
