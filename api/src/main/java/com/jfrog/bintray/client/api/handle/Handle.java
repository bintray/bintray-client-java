package com.jfrog.bintray.client.api.handle;

/**
 * @author Noam Y. Tenne
 */
public interface Handle<T> {

    String name();

    T get();
}