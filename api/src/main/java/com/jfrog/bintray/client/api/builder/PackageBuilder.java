package com.jfrog.bintray.client.api.builder;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class PackageBuilder {

    String description = null;
    List<String> labels = null;
    List<String> licenses = null;

    public PackageBuilder description(String description) {
        this.description = description;
        return this;
    }

    public PackageBuilder labels(List<String> labels) {
        this.labels = labels;
        return this;
    }

    public PackageBuilder licenses(List<String> licenses) {
        this.licenses = licenses;
        return this;
    }
}
