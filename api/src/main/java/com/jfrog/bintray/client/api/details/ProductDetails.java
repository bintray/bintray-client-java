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
 * pass to or receive from Bintray when performing actions on a product
 * NOTE: when serializing this class use getObjectMapper to obtain a suitable mapper for this class
 *
 * @author Dan Feldman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetails {

    //Properties marked with @JsonPropery here are serialized to the create \ update version requests, the rest are
    // only deserialized when getting the version info
    @JsonProperty
    String name;
    @JsonIgnore
    String owner;
    @JsonProperty(value = "desc")
    String description;
    @JsonIgnore
    DateTime created;
    @JsonProperty(value = "website_url")
    String websiteUrl;
    @JsonProperty(value = "vcs_url")
    String vcsUrl;
    @JsonProperty
    List<String> packages;
    @JsonIgnore
    List<String> versions;
    @JsonProperty(value = "sign_url_expiry")
    Integer signUrlExpiry;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getVcsUrl() {
        return vcsUrl;
    }

    public void setVcsUrl(String vcsUrl) {
        this.vcsUrl = vcsUrl;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    @JsonIgnore
    public List<String> getVersions() {
        return versions;
    }

    @JsonProperty
    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public Integer getSignUrlExpiry() {
        return signUrlExpiry;
    }

    public void setSignUrlExpiry(Integer signUrlExpiry) {
        this.signUrlExpiry = signUrlExpiry;
    }

    public static ObjectMapper getObjectMapper() {
        return ObjectMapperHelper.get();
    }
}
