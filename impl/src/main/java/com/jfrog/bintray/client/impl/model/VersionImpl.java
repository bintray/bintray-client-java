package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.model.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class VersionImpl implements Version {
    private static final Logger log = LoggerFactory.getLogger(VersionImpl.class);

    private String name;
    private String description;
    private String pkg;
    private String repository;
    private String owner;
    private List<String> labels;
    private List<String> attributeNames;
    private DateTime created;
    private DateTime updated;
    private DateTime released;
    private Integer ordinal;
    private String vcsTag;

    public VersionImpl() {
    }

    public VersionImpl(VersionDetails versionDetails) {
        this.name = versionDetails.getName();
        this.description = versionDetails.getDescription();
        this.pkg = versionDetails.getPkg();
        this.repository = versionDetails.getRepo();
        this.owner = versionDetails.getOwner();
        this.labels = versionDetails.getLabels();
        this.attributeNames = versionDetails.getAttributeNames();
        this.created = versionDetails.getCreated();
        this.updated = versionDetails.getUpdated();
        this.released = versionDetails.getReleased();
        this.ordinal = versionDetails.getOrdinal();
        this.vcsTag = versionDetails.getVcsTag();
    }

    public VersionImpl(String name, String description, String pkg, String repository, String owner, List<String> labels,
                       List<String> attributeNames, DateTime created, DateTime updated, DateTime released, int ordinal, String vcsTag) {
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
        this.vcsTag = vcsTag;
    }

    public static String getCreateUpdateJson(VersionDetails versionDetails) throws IOException {
        ObjectMapper mapper = ObjectMapperHelper.get();
        String jsonContent;
        try {
            jsonContent = mapper.writeValueAsString(versionDetails);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            log.debug("{}", e);
            throw e;
        }
        return jsonContent;
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

    public Integer ordinal() {
        return ordinal;
    }

    @Override
    public String vcsTag() {
        return vcsTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionImpl version = (VersionImpl) o;

        if (!name.equals(version.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Version{" +
                "getName='" + name + '\'' +
                ", description='" + description + '\'' +
                ", pkg='" + pkg + '\'' +
                ", repository='" + repository + '\'' +
                ", owner='" + owner + '\'' +
                ", labels=" + labels +
                ", attributeNames=" + attributeNames +
                ", created=" + created +
                ", updated=" + updated +
                ", released=" + released +
                ", ordinal=" + ordinal +
                '}';
    }
}
