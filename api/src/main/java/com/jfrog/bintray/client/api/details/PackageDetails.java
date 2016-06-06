package com.jfrog.bintray.client.api.details;

import com.jfrog.bintray.client.api.ObjectMapperHelper;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to serialize and deserialize the needed json to
 * pass to or receive from Bintray when performing actions on a package
 * NOTE: when serializing this class use getObjectMapper to obtain a suitable mapper for this class
 *
 * @author Dan Feldman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageDetails {

    //Properties marked with @JsonPropery here are serialized to the create \ update package requests, the rest are
    // only deserialized when getting the package info
    @JsonProperty(value = "name")
    private String name;
    @JsonIgnore
    private String repo;
    @JsonIgnore
    private String owner;
    @JsonIgnore
    private String subject;
    @JsonProperty(value = "desc")
    private String description;
    @JsonProperty(value = "labels")
    private List<String> labels;
    @JsonIgnore
    private List<String> attributeNames;
    @JsonIgnore
    private Integer followersCount;
    @JsonIgnore
    private DateTime created;
    @JsonProperty(value = "website_url")
    private String websiteUrl;
    @JsonProperty(value = "issue_tracker_url")
    private String issueTrackerUrl;
    @JsonProperty(value = "github_repo")
    private String gitHubRepo;
    @JsonProperty(value = "github_release_notes_file")
    private String releaseNotesFile;
    @JsonProperty(value = "public_download_numbers")
    private Boolean publicDownloadNumbers;
    @JsonProperty(value = "public_stats")
    private Boolean publicStats;
    @JsonIgnore
    private List<String> linkedRepos;
    @JsonIgnore
    private List<String> versions;
    @JsonProperty(value = "licenses")
    private List<String> licenses;
    @JsonIgnore
    private String latestVersion;
    @JsonIgnore
    private DateTime updated;
    @JsonIgnore
    private Integer rating;
    @JsonIgnore
    private Integer ratingCount;
    @JsonIgnore
    private List<String> systemIds;
    @JsonProperty(value = "vcs_url")
    private String vcsUrl;
    @JsonIgnore
    private List<Attribute> attributes;

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

    @JsonCreator
    public PackageDetails() {
    }

    public PackageDetails(String name) {
        this.name = name;
    }

    public static ObjectMapper getObjectMapper() {
        return ObjectMapperHelper.get();
    }

    public PackageDetails name(String name) {
        this.name = name;
        return this;
    }

    public PackageDetails description(String description) {
        this.description = description;
        return this;
    }

    public PackageDetails labels(List<String> labels) {
        this.labels = labels;
        return this;
    }

    public PackageDetails websiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }

    public PackageDetails issueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
        return this;
    }

    public PackageDetails gitHubRepo(String gitHubRepo) {
        this.gitHubRepo = gitHubRepo;
        return this;
    }

    public PackageDetails releaseNotesFile(String releaseNotesFile) {
        this.releaseNotesFile = releaseNotesFile;
        return this;
    }

    public PackageDetails publicDownloadNumbers(Boolean publicDownloadNumbers) {
        this.publicDownloadNumbers = publicDownloadNumbers;
        return this;
    }

    public PackageDetails publicStats(Boolean publicStats) {
        this.publicStats = publicStats;
        return this;
    }

    public PackageDetails licenses(List<String> licenses) {
        this.licenses = licenses;
        return this;
    }

    public PackageDetails vcsUrl(String vcsUrl) {
        this.vcsUrl = vcsUrl;
        return this;
    }

    public PackageDetails attributes(List<Attribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getRepo() {
        return repo;
    }

    @JsonProperty(value = "repo")
    public void setRepo(String repo) {
        this.repo = repo;
    }

    @JsonIgnore
    public String getOwner() {
        return owner;
    }

    @JsonProperty(value = "owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public String getSubject() {
        return subject;
    }

    @JsonProperty(value = "subject")
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @JsonIgnore
    public List<String> getAttributeNames() {
        return attributeNames;
    }

    @JsonProperty(value = "attribute_names")
    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    @JsonIgnore
    public Integer getFollowersCount() {
        return followersCount;
    }

    @JsonProperty(value = "followers_count")
    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    @JsonIgnore
    public DateTime getCreated() {
        return created;
    }

    @JsonProperty(value = "created")
    public void setCreated(DateTime created) {
        this.created = created;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
    }

    public String getGitHubRepo() {
        return gitHubRepo;
    }

    public void setGitHubRepo(String gitHubRepo) {
        this.gitHubRepo = gitHubRepo;
    }

    public String getReleaseNotesFile() {
        return releaseNotesFile;
    }

    public void setReleaseNotesFile(String releaseNotesFile) {
        this.releaseNotesFile = releaseNotesFile;
    }

    public Boolean getPublicDownloadNumbers() {
        return publicDownloadNumbers;
    }

    public void setPublicDownloadNumbers(Boolean publicDownloadNumbers) {
        this.publicDownloadNumbers = publicDownloadNumbers;
    }

    public Boolean getPublicStats() {
        return publicStats;
    }

    public void setPublicStats(Boolean publicStats) {
        this.publicStats = publicStats;
    }

    @JsonIgnore
    public List<String> getLinkedRepos() {
        return linkedRepos;
    }

    @JsonProperty("linked_to_repos")
    public void setLinkedRepos(List<String> linkedRepos) {
        this.linkedRepos = linkedRepos;
    }

    @JsonIgnore
    public List<String> getVersions() {
        return versions;
    }

    @JsonProperty("versions")
    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public List<String> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<String> licenses) {
        this.licenses = licenses;
    }

    @JsonIgnore
    public String getLatestVersion() {
        return latestVersion;
    }

    @JsonProperty(value = "latest_version")
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    @JsonIgnore
    public DateTime getUpdated() {
        return updated;
    }

    @JsonProperty(value = "updated")
    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    @JsonIgnore
    public Integer getRating() {
        return rating;
    }

    @JsonProperty(value = "rating")
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    @JsonIgnore
    public Integer getRatingCount() {
        return ratingCount;
    }

    @JsonProperty(value = "rating_count")
    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    @JsonIgnore
    public List<String> getSystemIds() {
        return systemIds;
    }

    @JsonProperty(value = "system_ids")
    public void setSystemIds(List<String> systemIds) {
        this.systemIds = systemIds;
    }

    public String getVcsUrl() {
        return vcsUrl;
    }

    public void setVcsUrl(String vcsUrl) {
        this.vcsUrl = vcsUrl;
    }

    @JsonIgnore
    public List<Attribute> getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}
