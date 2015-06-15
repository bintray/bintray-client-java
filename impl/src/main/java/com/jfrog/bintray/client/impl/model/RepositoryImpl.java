package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.model.Repository;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class RepositoryImpl implements Repository {
    private static final Logger log = LoggerFactory.getLogger(RepositoryImpl.class);

    private String name;
    private String owner;
    private String type;
    private Boolean isPrivate;
    private Boolean premium;
    private String desc;
    private List<String> labels;
    private DateTime created;
    private Integer packageCount;

    public RepositoryImpl() {
    }

    public RepositoryImpl(RepositoryDetails repositoryDetails) {
        this.name = repositoryDetails.getName();
        this.owner = repositoryDetails.getOwner();
        this.type = repositoryDetails.getType();
        this.isPrivate = repositoryDetails.getIsPrivate();
        this.premium = repositoryDetails.getPremium();
        this.desc = repositoryDetails.getDescription();
        this.labels = repositoryDetails.getLabels();
        this.created = repositoryDetails.getCreated();
        this.packageCount = repositoryDetails.getPackageCount();
    }

    public RepositoryImpl(String name, String owner, String type, Boolean isPrivate, Boolean premium, String desc,
                          List<String> labels, DateTime created, Integer packageCount) {
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.isPrivate = isPrivate;
        this.premium = premium;
        this.desc = desc;
        this.labels = labels;
        this.created = created;
        this.packageCount = packageCount;
    }

    public static String getCreateJson(RepositoryDetails repositoryDetails) throws IOException {
        ObjectMapper mapper = ObjectMapperHelper.get();
        try {
            return mapper.writeValueAsString(repositoryDetails);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            log.debug("{}", e);
            throw e;
        }
    }

    /**
     * PATCH repo only accepts description and label updates, name is needed for URL creation, because of the special
     * ignore and property structure of the RepositoryDetails class this method just uses a json generator to write
     * the update json.
     */
    public static String getUpdateJson(RepositoryDetails repositoryDetails) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator jGen = ObjectMapperHelper.get().getJsonFactory().createJsonGenerator(writer);
        jGen.writeStartObject();
        jGen.writeStringField("name", repositoryDetails.getName());
        jGen.writeStringField("desc", repositoryDetails.getDescription());
        if (repositoryDetails.getLabels().size() > 0) {
            jGen.writeArrayFieldStart("labels");
            for (String label : repositoryDetails.getLabels()) {
                jGen.writeString(label);
            }
            jGen.writeEndArray();
        }
        jGen.writeEndObject();
        jGen.close();
        writer.close();
        return writer.toString();
    }


    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getType() {
        return type;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public Boolean getPremium() {
        return premium;
    }

    public String getDesc() {
        return desc;
    }

    public List<String> getLabels() {
        return labels;
    }

    public DateTime getCreated() {
        return created;
    }

    public Integer getPackageCount() {
        return packageCount;
    }
}
