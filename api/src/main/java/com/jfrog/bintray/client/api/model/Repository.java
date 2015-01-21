package com.jfrog.bintray.client.api.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public interface Repository {

    String getName();

    String getOwner();

    String getDesc();

    List<String> getLabels();

    DateTime getCreated();

    Integer getPackageCount();
}