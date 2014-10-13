package com.jfrog.bintray.client.api.details;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class PackageDetails {

    String description;
    List<String> labels = null;
    List<String> licenses = null;
    String name;
    String vcsUrl;
    String websiteUrl;
    boolean publicDownloadNumbers;
    String issueTrackerUrl;

    public PackageDetails(String name) {
        this.name = name;
    }

    public PackageDetails description(String description) {
        this.description = description;
        return this;
    }

    public PackageDetails labels(List<String> labels) {
        this.labels = labels;
        return this;
    }

    public PackageDetails licenses(List<String> licenses) {
        this.licenses = licenses;
        return this;
    }

    public PackageDetails vcsUrl(String vcsUrl){
        this.vcsUrl = vcsUrl;
        return this;
    }
    public PackageDetails websiteUrl(String websiteUrl){
        this.websiteUrl = websiteUrl;
        return this;
    }

    public PackageDetails issueTrackerUrl(String issueTrackerUrl){
        this.issueTrackerUrl = issueTrackerUrl;
        return this;
    }

    public PackageDetails publicDownloadNumbers(boolean publicDownloadNumbers){
        this.publicDownloadNumbers = publicDownloadNumbers;
        return this;
    }
}
