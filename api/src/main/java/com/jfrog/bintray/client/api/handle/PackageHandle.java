package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.model.Pkg;

import java.io.IOException;
import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public interface PackageHandle extends Handle<Pkg> {

    RepositoryHandle repository();

    VersionHandle version(String versionName);

    Pkg get() throws IOException, BintrayCallException;

    PackageHandle update(PackageDetails packageDetails) throws IOException, BintrayCallException;

    PackageHandle delete() throws BintrayCallException;

    boolean exists() throws BintrayCallException;

    VersionHandle createVersion(VersionDetails versionDetails) throws IOException, BintrayCallException;

    PackageHandle setAttributes(PackageDetails packageDetails) throws IOException, BintrayCallException;

    PackageHandle setAttributes(List<Attribute> attributes) throws IOException, BintrayCallException;

    PackageHandle updateAttributes(PackageDetails packageDetails) throws IOException, BintrayCallException;

    PackageHandle updateAttributes(List<Attribute> attributes) throws IOException, BintrayCallException;

    String name();

    String getCurrentPackageUri();
}