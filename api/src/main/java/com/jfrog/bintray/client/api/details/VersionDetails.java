package com.jfrog.bintray.client.api.details;

import com.jfrog.bintray.client.api.ObjectMapperHelper;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import java.util.List;

/**
 * This class is used to serialize and deserialize the needed json to
 * pass to or receive from Bintray when performing actions on a subject
 * NOTE: when serializing this class use getObjectMapper to obtain a suitable mapper for this class
 *
 * @author Dan Feldman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionDetails {

    //Properties marked with @JsonPropery here are serialized to the create \ update version requests, the rest are
    // only deserialized when getting the version info
    @JsonProperty("name")
    String name;
    @JsonProperty("desc")
    String description;
    @JsonIgnore
    String pkg;
    @JsonIgnore
    String repo;
    @JsonIgnore
    String owner;
    @JsonIgnore
    List<String> labels;
    @JsonIgnore
    List<String> attributeNames;
    @JsonIgnore
    DateTime created;
    @JsonIgnore
    DateTime updated;
    @JsonProperty("released")
    DateTime released;
    @JsonIgnore
    Integer ordinal;
    @JsonIgnore
    List<Attribute> attributes;
    @JsonIgnore
    boolean gpgSign;
    @JsonProperty(value = "github_release_notes_file")
    private String releaseNotesFile;
    @JsonProperty(value = "github_use_tag_release_notes")
    private Boolean useTagReleaseNotes;
    @JsonProperty("vcs_tag")
    private String vcsTag;

    public VersionDetails() {

    }

    public VersionDetails(String name) {
        this.name = name;
    }

    public static ObjectMapper getObjectMapper() {
        return ObjectMapperHelper.get();
    }

    public VersionDetails description(String description) {
        this.description = description;
        return this;
    }

    public VersionDetails released(DateTime released) {
        this.released = released;
        return this;
    }

    public VersionDetails releaseNotesFile(String releaseNotesFile) {
        this.releaseNotesFile = releaseNotesFile;
        return this;
    }

    public VersionDetails useTagReleaseNotes(Boolean useTagReleaseNotes) {
        this.useTagReleaseNotes = useTagReleaseNotes;
        return this;
    }

    public VersionDetails vcsTag(String vcsTag) {
        this.vcsTag = vcsTag;
        return this;
    }

    public VersionDetails gpgSign(boolean gpgSign) {
        this.gpgSign = gpgSign;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public String getPkg() {
        return pkg;
    }

    @JsonProperty("package")
    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    @JsonIgnore
    public String getRepo() {
        return repo;
    }

    @JsonProperty("repo")
    public void setRepo(String repo) {
        this.repo = repo;
    }

    @JsonIgnore
    public String getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public List<String> getLabels() {
        return labels;
    }

    @JsonProperty("labels")
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @JsonIgnore
    public List<String> getAttributeNames() {
        return attributeNames;
    }

    @JsonProperty("attribute_names")
    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    @JsonIgnore
    public DateTime getCreated() {
        return created;
    }

    @JsonProperty("created")
    public void setCreated(DateTime created) {
        this.created = created;
    }

    @JsonIgnore
    public DateTime getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public DateTime getReleased() {
        return released;
    }

    public void setReleased(DateTime released) {
        this.released = released;
    }

    @JsonIgnore
    public Integer getOrdinal() {
        return ordinal;
    }

    @JsonProperty("ordinal")
    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getReleaseNotesFile() {
        return releaseNotesFile;
    }

    public void setReleaseNotesFile(String releaseNotesFile) {
        this.releaseNotesFile = releaseNotesFile;
    }

    public Boolean getUseTagReleaseNotes() {
        return useTagReleaseNotes;
    }

    public void setUseTagReleaseNotes(Boolean useTagReleaseNotes) {
        this.useTagReleaseNotes = useTagReleaseNotes;
    }

    public String getVcsTag() {
        return vcsTag;
    }

    public void setVcsTag(String vcsTag) {
        this.vcsTag = vcsTag;
    }

    @JsonIgnore
    public List<Attribute> getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @JsonIgnore
    public boolean isGpgSign() {
        return gpgSign;
    }

    @JsonProperty("gpgSign")
    public void setGpgSign(boolean gpgSign) {
        this.gpgSign = gpgSign;
    }
}