package com.jfrog.bintray.client.api.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public interface Version {

    String name();

    String description();

    String pkg();

    String repository();

    String owner();

    List<String> labels();

    List<String> attributeNames();

    DateTime created();

    DateTime updated();

    DateTime released();

    Integer ordinal();

    String vcsTag();

    Object getFieldByKey(String key);
}
