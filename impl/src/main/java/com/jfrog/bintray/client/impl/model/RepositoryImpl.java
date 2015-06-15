package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.details.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.model.Repository;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public static String getCreateUpdateJson(RepositoryDetails repositoryDetails) throws IOException {
        ObjectMapper mapper = ObjectMapperHelper.objectMapper;
        try {
            return mapper.writeValueAsString(repositoryDetails);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            log.debug("{}", e);
            throw e;
        }
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
