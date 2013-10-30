package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.builder.VersionBuilder;
import com.jfrog.bintray.client.api.model.Version;

/**
 * @author Noam Y. Tenne
 */
public interface VersionHandle extends Handle<Version> {

    PackageHandle pkg();

    VersionHandle create(VersionBuilder versionBuilder);

    VersionHandle update(VersionBuilder versionBuilder);

    VersionHandle delete();
}