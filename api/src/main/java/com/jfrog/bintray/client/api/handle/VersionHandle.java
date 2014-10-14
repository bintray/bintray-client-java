package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.model.Attribute;
import com.jfrog.bintray.client.api.model.Version;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Noam Y. Tenne
 */
public interface VersionHandle extends Handle<Version> {

    PackageHandle pkg();

    VersionHandle update(VersionDetails versionDetails);

    VersionHandle delete();

    VersionHandle setAttributes(List<Attribute> attributes);

    VersionHandle upload(Map<String, InputStream> content);

    VersionHandle publish();

    VersionHandle discard();

    VersionHandle sign(String passphrase);

    VersionHandle sign();
}