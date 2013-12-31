package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.model.Pkg;

/**
 * @author Noam Y. Tenne
 */
public interface PackageHandle extends Handle<Pkg> {

    RepositoryHandle repository();

    VersionHandle version(String name);

    PackageHandle update(PackageDetails packageDetails);

    PackageHandle delete();

    VersionHandle createVersion(VersionDetails versionDetails);

}
