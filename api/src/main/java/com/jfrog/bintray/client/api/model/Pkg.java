package com.jfrog.bintray.client.api.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public interface Pkg {

    String name();

    String repository();

    String owner();

    String description();

    List<String> labels();

    List<String> attributeNames();

    int rating();

    int ratingCount();

    int followersCount();

    DateTime created();

    List<String> versions();

    String latestVersion();

    DateTime updated();

    String linkedToRepo();
}
