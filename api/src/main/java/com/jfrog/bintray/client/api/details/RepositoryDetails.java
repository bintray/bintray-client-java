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
 * pass to or receive from Bintray when performing actions on a repository
 * NOTE: when serializing this class use getObjectMapper to obtain a suitable mapper for this class
 *
 * @author Dan Feldman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryDetails {

    //Properties marked with @JsonPropery here are serialized to the create \ update version requests, the rest are
    // only deserialized when getting the repo info
    @JsonIgnore
    String name;
    @JsonIgnore
    String owner;
    @JsonProperty
    String type;
    @JsonProperty(value = "private")
    Boolean isPrivate;
    @JsonProperty
    Boolean premium;
    @JsonProperty(value = "desc")
    String description;
    @JsonProperty
    List<String> labels;
    @JsonIgnore
    DateTime created;
    @JsonIgnore
    Integer packageCount;
    @JsonIgnore
    Boolean updateExisting; //Property is not used in the Bintray API but Artifactory uses is in it's Bintray integration

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

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonProperty(value = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public DateTime getCreated() {
        return created;
    }

    @JsonProperty(value = "created")
    public void setCreated(DateTime created) {
        this.created = created;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @JsonIgnore
    public Integer getPackageCount() {
        return packageCount;
    }

    @JsonProperty(value = "package_count")
    public void setPackageCount(Integer packageCount) {
        this.packageCount = packageCount;
    }

    @JsonIgnore
    public Boolean getUpdateExisting() {
        return updateExisting;
    }

    @JsonProperty(value = "updateExisting")
    public void setUpdateExisting(Boolean updateExisting) {
        this.updateExisting = updateExisting;
    }
}
