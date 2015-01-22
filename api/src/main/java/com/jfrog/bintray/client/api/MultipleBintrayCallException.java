package com.jfrog.bintray.client.api;

import java.util.ArrayList;
import java.util.List;

/**
 * An Exception that aggregates multiple BintrayCallExceptions for async operations
 *
 * @author danf
 */
public class MultipleBintrayCallException extends Exception {

    List<BintrayCallException> exceptions;

    public MultipleBintrayCallException() {
        super();
        exceptions = new ArrayList<>();
    }

    public MultipleBintrayCallException(List<BintrayCallException> exceptions) {
        super();
        this.exceptions = exceptions;
    }

    public List<BintrayCallException> getExceptions() {
        return exceptions;
    }


}