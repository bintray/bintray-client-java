package com.jfrog.bintray.client.api.handle;

import java.io.IOException;

/**
 * @author Noam Y. Tenne
 */
public interface Handle<T> {

    String name();

    T get() throws IOException;
}