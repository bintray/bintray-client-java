package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.details.ProductDetails;
import com.jfrog.bintray.client.api.model.Product;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * @author Dan Feldman
 */
public class ProductImpl implements Product {

    private String name;
    private String owner;
    private String description;
    private DateTime created;
    private String websiteUrl;
    private String vcsUrl;
    private List<String> packages;
    private List<String> versions;
    private Map<String, Object> other;

    public ProductImpl() {
    }

    public ProductImpl(ProductDetails productDetails) {
        this.name = productDetails.getName();
        this.owner = productDetails.getOwner();
        this.description = productDetails.getDescription();
        this.created = productDetails.getCreated();
        this.websiteUrl = productDetails.getWebsiteUrl();
        this.vcsUrl = productDetails.getVcsUrl();
        this.packages = productDetails.getPackages();
        this.versions = productDetails.getVersions();
        this.other = productDetails.other();
    }

    public ProductImpl(String name, String owner, String description, List<String> packages, List<String> versions,
            DateTime created, String websiteUrl, String vcsUrl) {
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.created = created;
        this.websiteUrl = websiteUrl;
        this.vcsUrl = vcsUrl;
        this.packages = packages;
        this.versions = versions;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public DateTime getCreated() {
        return created;
    }

    @Override
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    @Override
    public String getVcsUrl() {
        return vcsUrl;
    }

    @Override
    public List<String> getPackages() {
        return packages;
    }

    @Override
    public List<String> getVersions() {
        return versions;
    }

    @Override
    public Object getFieldByKey(String key) {
        return other.get(key);
    }
}
