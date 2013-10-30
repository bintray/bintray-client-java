package com.jfrog.bintray.client.api.handle;

import java.io.Closeable;

/**
 * @author Noam Y. Tenne
 */
public interface Bintray extends Closeable {

    String uri();

    SubjectHandle currentSubject();

    SubjectHandle subject(String subject);

    RepositoryHandle repository(String repositoryPath);

    PackageHandle pkg(String packagePath);

    VersionHandle version(String versionPath);

//    Search search();
}