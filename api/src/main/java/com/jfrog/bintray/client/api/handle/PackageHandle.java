package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.builder.PackageBuilder;
import com.jfrog.bintray.client.api.model.Pkg;

/**
 * @author Noam Y. Tenne
 */
public interface PackageHandle extends Handle<Pkg> {

    RepositoryHandle repository();

    VersionHandle version(String name);

    PackageHandle create(PackageBuilder packageBuilder);

    PackageHandle update(PackageBuilder packageBuilder);

    PackageHandle delete();
}
