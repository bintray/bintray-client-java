package com.jfrog.bintray.client.api.details;

import com.jfrog.bintray.client.api.ObjectMapperHelper;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to serialize and deserialize the needed json to
 * pass to or receive from Bintray when performing actions on a subject
 * NOTE: when serializing this class use getObjectMapper to obtain a suitable mapper for this class
 *
 * @author Dan Feldman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectDetails {

    @JsonProperty("name")
    String name;
    @JsonProperty("full_name")
    String fullName;
    @JsonProperty("gravatar_id")
    String gravatarId;
    @JsonProperty("repos")
    List<String> repos;
    @JsonProperty("organizations")
    List<String> organizations;
    @JsonProperty("followers_count")
    Integer followersCount;
    @JsonProperty("registered")
    DateTime registered;
    @JsonProperty("quota_used_bytes")
    Long quotaUsedBytes;

    //All other props that don't have specific fields
    private Map<String, Object> other = new HashMap<>();

    @JsonAnySetter
    public void set(String name, Object value) {
        other.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> other() {
        return other;
    }

    public static ObjectMapper getObjectMapper() {
        return ObjectMapperHelper.get();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGravatarId() {
        return gravatarId;
    }

    public void setGravatarId(String gravatarId) {
        this.gravatarId = gravatarId;
    }

    public List<String> getRepos() {
        return repos;
    }

    public void setRepos(List<String> repos) {
        this.repos = repos;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public DateTime getRegistered() {
        return registered;
    }

    public void setRegistered(DateTime registered) {
        this.registered = registered;
    }

    public Long getQuotaUsedBytes() {
        return quotaUsedBytes;
    }

    public void setQuotaUsedBytes(Long quotaUsedBytes) {
        this.quotaUsedBytes = quotaUsedBytes;
    }
}
