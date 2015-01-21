package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.model.Repository;

import java.io.IOException;

/**
 * @author Noam Y. Tenne
 */
public interface RepositoryHandle extends Handle<Repository> {

    SubjectHandle owner();

    PackageHandle pkg(String name);

    PackageHandle createPkg(PackageDetails packageDetails) throws IOException, BintrayCallException;

    AttributesSearchQuery searchForPackage();

    boolean exists() throws BintrayCallException;

    String getRepositoryUri();
}