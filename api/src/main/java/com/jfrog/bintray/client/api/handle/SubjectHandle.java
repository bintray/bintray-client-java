package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.model.Subject;

/**
 * @author Noam Y. Tenne
 */
public interface SubjectHandle extends Handle<Subject> {

    RepositoryHandle repository(String name);
}