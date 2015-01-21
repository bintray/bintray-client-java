package com.jfrog.bintray.client.api;

import org.apache.http.client.HttpResponseException;

/**
 * An exception thrown for failed operations against the Bintray api.
 *
 * @author danf
 */
public class BintrayCallException extends HttpResponseException {

    private int statusCode;
    private String reason;
    private String message;

    public BintrayCallException(String message, int statusCode, String reason) {
        super(statusCode, message);
        this.statusCode = statusCode;
        this.reason = reason;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return statusCode + " , " + reason + " : " + message;
    }

}