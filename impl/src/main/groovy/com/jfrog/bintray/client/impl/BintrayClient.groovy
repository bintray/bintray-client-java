package com.jfrog.bintray.client.impl

import com.jfrog.bintray.client.api.handle.Bintray
import com.jfrog.bintray.client.impl.handle.BintrayImpl
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.apache.http.client.params.CookiePolicy

import static org.apache.http.auth.params.AuthPNames.TARGET_AUTH_PREF;
import static org.apache.http.client.params.AuthPolicy.BASIC
import static org.apache.http.client.params.CookiePolicy.IGNORE_COOKIES;
import static org.apache.http.client.params.HttpClientParams.setCookiePolicy

/**
 * @author Noam Y. Tenne
 */
public class BintrayClient {

    static Bintray create(String username, String apiKey) {
        create("https://api.bintray.com", username, apiKey)
    }

    static Bintray create(String url, String username, String apiKey) {
        try {
            RESTClient restClient = new RESTClient(url.endsWith("/") ?: "$url/", ContentType.JSON);
            restClient.headers.'User-Agent' = 'Bintray-Client/1.0'
            def params = restClient.client.getParams()
            setCookiePolicy params, IGNORE_COOKIES
            if (username != null && apiKey != null) {
                restClient.auth.basic username, apiKey
                params.setParameter TARGET_AUTH_PREF, [BASIC]
            }
            new BintrayImpl(restClient)
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("It seems an invalid URI has been provided", e);
        }
    }

    static Bintray create() {
        return create(null, null)
    }
}
