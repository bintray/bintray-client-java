package com.jfrog.bintray.client.impl;

import com.jfrog.bintray.client.api.handle.Bintray;
import com.jfrog.bintray.client.impl.handle.BintrayImpl;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Creates a client to perform api actions with, can be configured with\without proxy (can be passed as null)
 * By default, https://api.bintray.com is used, unless specified otherwise (i.e. can be configured to work
 * with https://dl.bintray.com/).
 *
 * @author Dan Feldman
 */
public class BintrayClient {

    public static final int DEFAULT_TIMEOUT = 15000;
    public static final String BINTRAY_API_URL = "https://api.bintray.com";
    public static final String USER_AGENT = "BintrayJavaClient/0.5"; // TODO: make dynamic

    //Mainly used by Artifactory to avoid all of the configuration, but you can specify your own too
    static public Bintray create(CloseableHttpClient preConfiguredClient, String url) {
        return new BintrayImpl(preConfiguredClient, url);
    }

    /**
     * Username and API key, no proxy
     */
    static public Bintray create(String userName, String apiKey) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, apiKey);
        return new BintrayImpl(createClient(creds, null, BINTRAY_API_URL), BINTRAY_API_URL);
    }

    /**
     * Username, API key, and custom url
     */
    static public Bintray create(String url, String userName, String apiKey) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, apiKey);
        return new BintrayImpl(createClient(creds, null, BINTRAY_API_URL), BINTRAY_API_URL);
    }

    /**
     * Credentials with proxy
     */
    static public Bintray create(UsernamePasswordCredentials creds, HttpClientConfigurator.ProxyConfig proxyConfig) {
        return new BintrayImpl(createClient(creds, proxyConfig, BINTRAY_API_URL), BINTRAY_API_URL);
    }

    /**
     * Username, API key, proxy and custom url
     */
    static public Bintray create(String bintrayUserName, String bintrayApiKey,
                                 HttpClientConfigurator.ProxyConfig proxyConfig, String url) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(bintrayUserName, bintrayApiKey);
        return new BintrayImpl(createClient(creds, proxyConfig, url), url);
    }


    private static CloseableHttpClient createClient(UsernamePasswordCredentials creds,
                                                    HttpClientConfigurator.ProxyConfig proxyConfig, String url) {

        String baseUrl = (url == null || url.isEmpty()) ? BINTRAY_API_URL : url;
        return new HttpClientConfigurator()
                .hostFromUrl(baseUrl)
                .soTimeout(DEFAULT_TIMEOUT)
                .connectionTimeout(DEFAULT_TIMEOUT)
                .noRetry()
                .proxy(proxyConfig)
                .authentication(creds)
                .getClient();
    }
}
