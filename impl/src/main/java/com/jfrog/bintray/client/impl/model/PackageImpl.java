package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.model.Pkg;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Noam Y. Tenne
 */
public class PackageImpl implements Pkg {
    private static final Logger log = LoggerFactory.getLogger(PackageImpl.class);

    private String name;
    private String repository;
    private String owner;
    private String description;
    private List<String> labels;
    private List<String> attributeNames;
    private Integer rating;
    private Integer ratingCount;
    private Integer followersCount;
    private DateTime created;
    private List<String> versions;
    private String latestVersion;
    private DateTime updated;
    private List<String> linkedToRepos;
    private List<String> systemIds;
    private Map<String, Object> other;

    public PackageImpl(PackageDetails packageDetails) {
        this.name = packageDetails.getName();
        this.repository = packageDetails.getRepo();
        this.owner = packageDetails.getOwner();
        this.description = packageDetails.getDescription();
        this.labels = packageDetails.getLabels();
        this.attributeNames = packageDetails.getAttributeNames();
        this.ratingCount = packageDetails.getRatingCount();
        this.followersCount = packageDetails.getFollowersCount();
        this.created = packageDetails.getCreated();
        this.versions = packageDetails.getVersions();
        this.latestVersion = packageDetails.getLatestVersion();
        this.updated = packageDetails.getUpdated();
        this.linkedToRepos = packageDetails.getLinkedRepos();
        this.systemIds = packageDetails.getSystemIds();
        this.other = packageDetails.other();
    }

    public PackageImpl(String name, String repository, String owner, String description, List<String> labels,
                       List<String> attributeNames, Integer rating, Integer ratingCount, Integer followersCount,
                       DateTime created, List<String> versions, String latestVersion, DateTime updated,
                       List<String> linkedToRepos, List<String> systemIds) {
        this.name = name;
        this.repository = repository;
        this.owner = owner;
        this.description = description;
        this.labels = labels;
        this.attributeNames = attributeNames;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.followersCount = followersCount;
        this.created = created;
        this.versions = versions;
        this.latestVersion = latestVersion;
        this.updated = updated;
        this.linkedToRepos = linkedToRepos;
        this.systemIds = systemIds;
    }

    public static String getCreateUpdateJson(PackageDetails packageDetails) throws IOException {
        ObjectMapper mapper = ObjectMapperHelper.get();
        String jsonContent;
        try {
            jsonContent = mapper.writeValueAsString(packageDetails);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            throw e;
        }
        return jsonContent;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String repository() {
        return repository;
    }

    @Override
    public String owner() {
        return owner;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public List<String> labels() {
        return labels;
    }

    @Override
    public List<String> attributeNames() {
        return attributeNames;
    }

    @Override
    public Integer rating() {
        return rating;
    }

    @Override
    public Integer ratingCount() {
        return ratingCount;
    }

    @Override
    public Integer followersCount() {
        return followersCount;
    }

    @Override
    public DateTime created() {
        return created;
    }

    @Override
    public List<String> versions() {
        return versions;
    }

    @Override
    public String latestVersion() {
        return latestVersion;
    }

    @Override
    public DateTime updated() {
        return updated;
    }

    @Override
    public List<String> linkedToRepos() {
        return linkedToRepos;
    }

    @Override
    public List<String> systemIds() {
        return systemIds;
    }

    @Override
    public Object getFieldByKey(String key) {
        return other.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PackageImpl aPackage = (PackageImpl) o;

        if (!name.equals(aPackage.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Package{" +
                "getName='" + name + '\'' +
                ", repository='" + repository + '\'' +
                ", owner='" + owner + '\'' +
                ", description='" + description + '\'' +
                ", labels=" + labels +
                ", attributeNames=" + attributeNames +
                ", rating=" + rating +
                ", ratingCount=" + ratingCount +
                ", followersCount=" + followersCount +
                ", created=" + created +
                ", versions=" + versions +
                ", latestVersion='" + latestVersion + '\'' +
                ", updated=" + updated +
                ", linkedToRepos='" + linkedToRepos + '\'' +
                ", systemIds=" + systemIds +
                '}';
    }
}
