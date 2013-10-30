package com.jfrog.bintray.client.api.model;

import org.joda.time.DateTime;

import java.util.Collection;

/**
 * @author Noam Y. Tenne
 */
public interface Subject {

    String getName();

    String getFullName();

    String getGravatarId();

    Collection<String> getRepositories();

    Collection<String> getOrganizations();

    int getFollowersCount();

    DateTime getRegistered();

    long getQuotaUsedBytes();
}
