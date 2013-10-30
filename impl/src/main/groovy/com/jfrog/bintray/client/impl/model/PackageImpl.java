package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.model.Pkg;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class PackageImpl implements Pkg {

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
    private String linkedToRepo;

    public PackageImpl() {
    }

    public PackageImpl(String name, String repository, String owner, String description, List<String> labels,
                       List<String> attributeNames, Integer rating, Integer ratingCount, Integer followersCount,
                       DateTime created, List<String> versions, String latestVersion, DateTime updated,
                       String linkedToRepo) {
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
        this.linkedToRepo = linkedToRepo;
    }

    public String name() {
        return name;
    }

    public String repository() {
        return repository;
    }

    public String owner() {
        return owner;
    }

    public String description() {
        return description;
    }

    public List<String> labels() {
        return labels;
    }

    public List<String> attributeNames() {
        return attributeNames;
    }

    public int rating() {
        return rating;
    }

    public int ratingCount() {
        return ratingCount;
    }

    public int followersCount() {
        return followersCount;
    }

    public DateTime created() {
        return created;
    }

    public List<String> versions() {
        return versions;
    }

    public String latestVersion() {
        return latestVersion;
    }

    public DateTime updated() {
        return updated;
    }

    public String linkedToRepo() {
        return linkedToRepo;
    }
}
