package com.jfrog.bintray.client.api.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Dan Feldman
 */
public interface Product {

    String getName();

    String getOwner();

    String getDescription();

    DateTime getCreated();

    String getWebsiteUrl();

    String getVcsUrl();

    List<String> getPackages();

    List<String> getVersions();
}