/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.jfrog.bintray.client.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for HTTP client.
 *
 * @author Yossi Shaul
 */
@SuppressWarnings("deprecation")
public class HttpClientConfigurator {
    private static final Logger log = LoggerFactory.getLogger(HttpClientConfigurator.class);

    private HttpClientBuilder builder = HttpClients.custom();
    private RequestConfig.Builder config = RequestConfig.custom();
    private String host;
    private BasicCredentialsProvider credsProvider;
    private int maxConnectionsPerRoute = 30; //Default
    private int maxTotalConnections = 50;   //Default

    public HttpClientConfigurator() {
        builder.setUserAgent(BintrayClient.USER_AGENT);
        credsProvider = new BasicCredentialsProvider();
    }

    public CloseableHttpClient getClient() {
        if (hasCredentials()) {
            builder.setDefaultCredentialsProvider(credsProvider);
        }
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();   //Threadsafe
        cm.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        cm.setMaxTotal(maxTotalConnections);
        builder.setConnectionManager(cm);

        return builder.setDefaultRequestConfig(config.build()).build();
    }

    /**
     * May throw a runtime exception when the given URL is invalid.
     */
    public HttpClientConfigurator hostFromUrl(String urlStr) throws IllegalArgumentException {
        if (StringUtils.isNotBlank(urlStr)) {
            try {
                URL url = new URL(urlStr);
                host(url.getHost());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Cannot parse the url " + urlStr, e);
            }
        }
        return this;
    }

    /**
     * Ignores blank getValues
     */
    public HttpClientConfigurator host(String host) {
        if (StringUtils.isNotBlank(host)) {
            this.host = host;
            builder.setRoutePlanner(new DefaultHostRoutePlanner(host));
        }
        return this;
    }

    public HttpClientConfigurator defaultMaxConnectionsPerHost(int maxConnectionsPerHost) {
        //Actually this is overridden by the ConnectionManager's configuration (set by maxConnectionsPerRoute), test if it can be removed
        builder.setMaxConnPerRoute(maxConnectionsPerHost);
        this.maxConnectionsPerRoute = maxConnectionsPerHost;
        return this;
    }

    public HttpClientConfigurator maxTotalConnections(int maxTotalConnections) {
        //Actually this is overridden by the ConnectionManager's configuration (set by maxTotalConnections), test if it can be removed
        builder.setMaxConnTotal(maxTotalConnections);
        this.maxTotalConnections = maxTotalConnections;
        return this;
    }

    public HttpClientConfigurator connectionTimeout(int connectionTimeout) {
        config.setConnectTimeout(connectionTimeout);
        return this;
    }

    public HttpClientConfigurator soTimeout(int soTimeout) {
        config.setSocketTimeout(soTimeout);
        return this;
    }

    public HttpClientConfigurator noCookies() {
        builder.disableCookieManagement();
        return this;
    }

    /**
     * see {@link org.apache.http.client.config.RequestConfig#isStaleConnectionCheckEnabled()}
     */
    public HttpClientConfigurator staleCheckingEnabled(boolean staleCheckingEnabled) {
        config.setStaleConnectionCheckEnabled(staleCheckingEnabled);
        return this;
    }

    /**
     * Disable request retries on service unavailability.
     */
    public HttpClientConfigurator noRetry() {
        return retry(0, false);
    }

    /**
     * Number of retry attempts. Default is 3 retries.
     *
     * @param retryCount Number of retry attempts. 0 means no retries.
     */
    public HttpClientConfigurator retry(int retryCount, boolean requestSentRetryEnabled) {
        if (retryCount == 0) {
            builder.disableAutomaticRetries();
        } else {
            builder.setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, requestSentRetryEnabled));
        }
        return this;
    }

    /**
     * Ignores blank or invalid input
     */
    public HttpClientConfigurator localAddress(String localAddress) {
        if (StringUtils.isNotBlank(localAddress)) {
            try {
                InetAddress address = InetAddress.getByName(localAddress);
                config.setLocalAddress(address);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Invalid local address: " + localAddress, e);
            }
        }
        return this;
    }

    /**
     * Ignores null credentials
     */
    public HttpClientConfigurator authentication(UsernamePasswordCredentials creds) {
        if (creds != null) {
            authentication(creds.getUserName(), creds.getPassword());
        }

        return this;
    }

    /**
     * Ignores blank username input
     */
    public HttpClientConfigurator authentication(String username, String password) {
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(host)) {
                throw new IllegalStateException("Cannot configure authentication when host is not set.");
            }
            credsProvider.setCredentials(
                    new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
                    new UsernamePasswordCredentials(username, password));

            builder.addInterceptorFirst(new PreemptiveAuthInterceptor());
        }
        return this;
    }

    public HttpClientConfigurator proxy(ProxyConfig proxyConfig) {
        configureProxy(proxyConfig);
        return this;
    }

    private void configureProxy(ProxyConfig proxy) {
        if (proxy != null) {
            config.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
            if (proxy.getUserName() != null) {
                Credentials creds = null;
                if (proxy.getNtDomain() == null) {
                    creds = new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword());
                    List<String> authPrefs = Arrays.asList(AuthSchemes.DIGEST, AuthSchemes.BASIC, AuthSchemes.NTLM);
                    config.setProxyPreferredAuthSchemes(authPrefs);

                    // preemptive proxy authentication
                    builder.addInterceptorFirst(new ProxyPreemptiveAuthInterceptor());
                } else {
                    try {
                        String ntHost =
                                StringUtils.isBlank(proxy.getNtHost()) ? InetAddress.getLocalHost().getHostName() :
                                        proxy.getNtHost();
                        creds = new NTCredentials(proxy.getUserName(), proxy.getPassword(), ntHost, proxy.getNtDomain());
                    } catch (UnknownHostException e) {
                        log.error("Failed to determine required local hostname for NTLM credentials.", e);
                    }
                }
                if (creds != null) {
                    credsProvider.setCredentials(
                            new AuthScope(proxy.getHost(), proxy.getPort(), AuthScope.ANY_REALM), creds);
                    if (proxy.getRedirectToHosts() != null) {
                        for (String hostName : proxy.getRedirectToHosts()) {
                            credsProvider.setCredentials(
                                    new AuthScope(hostName, AuthScope.ANY_PORT, AuthScope.ANY_REALM), creds);
                        }
                    }
                }
            }
        }
    }

    private boolean hasCredentials() {
        return credsProvider.getCredentials(AuthScope.ANY) != null;
    }


    static class DefaultHostRoutePlanner extends DefaultRoutePlanner {
        private final HttpHost defaultHost;

        public DefaultHostRoutePlanner(String defaultHost) {
            super(DefaultSchemePortResolver.INSTANCE);
            this.defaultHost = new HttpHost(defaultHost);
        }

        @Override
        public HttpRoute determineRoute(HttpHost host, HttpRequest request, HttpContext context) throws HttpException {
            if (host == null) {
                host = defaultHost;
            }
            return super.determineRoute(host, request, context);
        }

        public HttpHost getDefaultHost() {
            return defaultHost;
        }
    }

    static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            AuthState authState = clientContext.getTargetAuthState();

            // If there's no auth scheme available yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
                HttpHost targetHost = clientContext.getTargetHost();
                Credentials creds = credsProvider.getCredentials(
                        new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                if (creds == null) {
                    throw new HttpException("No credentials for preemptive authentication");
                }
                authState.update(new BasicScheme(), creds);
            }
        }
    }

    static class ProxyPreemptiveAuthInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            AuthState proxyAuthState = clientContext.getProxyAuthState();

            // If there's no auth scheme available yet, try to initialize it preemptively
            if (proxyAuthState.getAuthScheme() == null) {
                CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
                RouteInfo route = clientContext.getHttpRoute();
                if (route == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("No route found for {}", clientContext.getTargetHost());
                    }
                    return;
                }

                HttpHost proxyHost = route.getProxyHost();
                if (proxyHost == null) {
                    log.warn("No proxy host found in route {} for host {}", route, clientContext.getTargetHost());
                    return;
                }

                Credentials creds = credsProvider.getCredentials(
                        new AuthScope(proxyHost.getHostName(), proxyHost.getPort()));
                if (creds == null) {
                    log.info("No credentials found for proxy: " + proxyHost);
                    return;
                }
                proxyAuthState.update(new BasicScheme(ChallengeState.PROXY), creds);
            }
        }
    }


    public static class ProxyConfig {
        String host;
        int port;
        String userName;
        String password;
        String ntHost;
        String ntDomain;
        List<String> redirectToHosts;

        public String getHost() {
            return host;
        }

        public ProxyConfig host(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public ProxyConfig port(int port) {
            this.port = port;
            return this;
        }

        public String getUserName() {
            return userName;
        }

        public ProxyConfig userName(String userName) {
            this.userName = userName;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public ProxyConfig password(String password) {
            this.password = password;
            return this;
        }

        public String getNtHost() {
            return ntHost;
        }

        public ProxyConfig ntHost(String ntHost) {
            this.ntHost = ntHost;
            return this;
        }

        public String getNtDomain() {
            return ntDomain;
        }

        public ProxyConfig ntDomain(String ntDomain) {
            this.ntDomain = ntDomain;
            return this;
        }

        public List<String> getRedirectToHosts() {
            return redirectToHosts;
        }

        public ProxyConfig redirectToHosts(List<String> redirectToHosts) {
            this.redirectToHosts = redirectToHosts;
            return this;
        }
    }
}
