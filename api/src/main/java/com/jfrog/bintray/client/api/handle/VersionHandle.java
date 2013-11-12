package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.model.Version;

/**
 * @author Noam Y. Tenne
 */
public interface VersionHandle extends Handle<Version> {

    PackageHandle pkg();

    VersionHandle update(VersionDetails versionDetails);

    VersionHandle delete();
}