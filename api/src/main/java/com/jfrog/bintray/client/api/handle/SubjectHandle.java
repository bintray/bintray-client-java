package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.model.Subject;

import java.io.IOException;

/**
 * @author Noam Y. Tenne
 */
public interface SubjectHandle extends Handle<Subject> {

    String name();

    Subject get() throws IOException, BintrayCallException;

    RepositoryHandle repository(String name);
}