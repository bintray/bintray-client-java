package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.model.Version;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class VersionImpl implements Version {

    String name;
    String description;
    String pkg;
    String repository;
    String owner;
    private List<String> labels;
    List<String> attributeNames;
    private DateTime created;
    private DateTime updated;
    DateTime released;
    int ordinal;

    public VersionImpl() {
    }

    public VersionImpl(String name, String description, String pkg, String repository, String owner, List<String> labels,
                       List<String> attributeNames, DateTime created, DateTime updated, DateTime released, int ordinal) {
        this.name = name;
        this.description = description;
        this.pkg = pkg;
        this.repository = repository;
        this.owner = owner;
        this.labels = labels;
        this.attributeNames = attributeNames;
        this.created = created;
        this.updated = updated;
        this.released = released;
        this.ordinal = ordinal;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String pkg() {
        return pkg;
    }

    public String repository() {
        return repository;
    }

    public String owner() {
        return owner;
    }

    public List<String> labels() {
        return labels;
    }

    public List<String> attributeNames() {
        return attributeNames;
    }

    public DateTime created() {
        return created;
    }

    public DateTime updated() {
        return updated;
    }

    public DateTime released() {
        return released;
    }

    public int ordinal() {
        return ordinal;
    }
}
