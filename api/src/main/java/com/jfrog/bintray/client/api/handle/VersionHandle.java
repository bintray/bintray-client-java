package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.MultipleBintrayCallException;
import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.model.Version;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Noam Y. Tenne
 */
public interface VersionHandle extends Handle<Version> {

    PackageHandle pkg();

    Version get() throws IOException, BintrayCallException;

    VersionHandle update(VersionDetails versionDetails) throws IOException, BintrayCallException;

    VersionHandle delete() throws BintrayCallException;

    boolean exists() throws BintrayCallException;

    VersionHandle setAttributes(VersionDetails versionDetails) throws IOException, BintrayCallException;

    VersionHandle setAttributes(List<Attribute> attributes) throws IOException, BintrayCallException;

    VersionHandle updateAttributes(VersionDetails versionDetails) throws IOException, BintrayCallException;

    VersionHandle updateAttributes(List<Attribute> attributes) throws IOException, BintrayCallException;

    VersionHandle upload(Map<String, InputStream> content) throws MultipleBintrayCallException;

    VersionHandle upload(String path, InputStream content) throws BintrayCallException;

    VersionHandle publish() throws BintrayCallException;

    VersionHandle publishSync() throws BintrayCallException

    VersionHandle discard() throws BintrayCallException;

    VersionHandle sign(String passphrase, int fileCount) throws BintrayCallException;

    VersionHandle sign(int fileCount) throws BintrayCallException;

    String getVersionUri();
}