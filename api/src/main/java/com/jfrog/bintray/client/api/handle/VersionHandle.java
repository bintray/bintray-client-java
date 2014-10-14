package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.model.Attribute;
import com.jfrog.bintray.client.api.model.Version;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public interface VersionHandle extends Handle<Version> {

    PackageHandle pkg();

    VersionHandle update(VersionDetails versionDetails);

    VersionHandle delete();

    VersionHandle setAttributes(List<Attribute> attributes);
}