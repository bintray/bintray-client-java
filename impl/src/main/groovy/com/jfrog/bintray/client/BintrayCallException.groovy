package com.jfrog.bintray.client

import groovy.transform.Canonical
import org.apache.http.client.HttpResponseException

/**
 * @author jbaruch
 * @since 05/01/14
 */

@Canonical
public class BintrayCallException extends HttpResponseException {

    int statusCode

    String reason

    String message

    public BintrayCallException(String message, int statusCode, String reason) {
        super(statusCode, reason)
        this.statusCode = statusCode
        this.reason = reason
        this.message = message
    }
}
