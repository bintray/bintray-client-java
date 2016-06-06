package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.details.SubjectDetails;
import com.jfrog.bintray.client.api.model.Subject;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Map;

/**
 * @author Noam Y. Tenne
 */
public class SubjectImpl implements Subject {

    private String name;
    private String fullName;
    private String gravatarId;
    private Collection<String> repositories;
    private Collection<String> organizations;
    private Integer followersCount;
    private DateTime registered;
    private Long quotaUsedBytes;
    private Map<String, Object> other;

    public SubjectImpl() {
    }

    public SubjectImpl(SubjectDetails subjectDetails) {
        this.name = subjectDetails.getName();
        this.fullName = subjectDetails.getFullName();
        this.gravatarId = subjectDetails.getGravatarId();
        this.repositories = subjectDetails.getRepos();
        this.organizations = subjectDetails.getOrganizations();
        this.followersCount = subjectDetails.getFollowersCount();
        this.registered = subjectDetails.getRegistered();
        this.quotaUsedBytes = subjectDetails.getQuotaUsedBytes();
        this.other = subjectDetails.other();
    }

    public SubjectImpl(String name, String fullName, String gravatarId, Collection<String> repositories,
                       Collection<String> organizations, Integer followersCount, DateTime registered, Long quotaUsedBytes) {
        this.name = name;
        this.fullName = fullName;
        this.gravatarId = gravatarId;
        this.repositories = repositories;
        this.organizations = organizations;
        this.followersCount = followersCount;
        this.registered = registered;
        this.quotaUsedBytes = quotaUsedBytes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getGravatarId() {
        return gravatarId;
    }

    @Override
    public Collection<String> getRepositories() {
        return repositories;
    }

    @Override
    public Collection<String> getOrganizations() {
        return organizations;
    }

    @Override
    public Integer getFollowersCount() {
        return followersCount;
    }

    @Override
    public DateTime getRegistered() {
        return registered;
    }

    @Override
    public Long getQuotaUsedBytes() {
        return quotaUsedBytes;
    }

    @Override
    public Object getFieldByKey(String key) {
        return other.get(key);
    }
}
