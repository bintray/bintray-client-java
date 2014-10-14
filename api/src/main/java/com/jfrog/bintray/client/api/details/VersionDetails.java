package com.jfrog.bintray.client.api.details;

import org.joda.time.DateTime;

/**
 * @author Noam Y. Tenne
 */
public class VersionDetails {

    String description = null;
    DateTime released = null;
    String name;
    String vcsTag;

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

    public VersionDetails vcsTag(String vcsTag) {
        this.vcsTag = vcsTag;
        return this;
    }
}