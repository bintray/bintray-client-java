package com.jfrog.bintray.client.impl;

import com.jfrog.bintray.client.api.handle.Bintray;
import com.jfrog.bintray.client.impl.handle.BintrayImpl;
import groovyx.net.http.ContentType;
import groovyx.net.http.RESTClient;

import java.net.URISyntaxException;

/**
 * @author Noam Y. Tenne
 */
public class BintrayClient {

    public static Bintray create(String username, String password) {
        return create("https://api.bintray.com", username, password);
    }

    public static Bintray create(String url, String username, String apiKey) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        RESTClient restClient = null;
        try {
            restClient = new RESTClient(url, ContentType.JSON);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("It seems an invalid URI has been provided", e);
        }
        restClient.getAuth().basic(username, apiKey);
        return new BintrayImpl(restClient);
    }
}
