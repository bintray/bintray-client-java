package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.model.Pkg;
import com.jfrog.bintray.client.api.model.Repository;

/**
 * @author Noam Y. Tenne
 */
public interface RepositoryHandle extends Handle<Repository> {

    SubjectHandle owner();

    PackageHandle pkg(String name);

    PackageHandle createPkg(PackageDetails builder);

    ArtibutesSearchQuery searchForPackage();
}
