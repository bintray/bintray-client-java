package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.handle.*
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import org.apache.http.auth.AuthScope
import org.apache.http.client.methods.HttpPatch
/**
 * @author Noam Y. Tenne
 */
class BintrayImpl implements Bintray {

    private final RESTClient restClient

    BintrayImpl(RESTClient restClient) {
        this.restClient = restClient
    }

    String uri() {
        restClient.uri.toString()
    }

    SubjectHandle currentSubject() {
        String authenticatingSubject =
                restClient.auth.builder.client.credentialsProvider.getCredentials(AuthScope.ANY).userPrincipal.name
        subject(authenticatingSubject)
    }

    SubjectHandle subject(String subject) {
        new SubjectHandleImpl(this, subject)
    }

    RepositoryHandle repository(String repositoryPath) {
        return null
    }

    PackageHandle pkg(String packagePath) {
        return null
    }

    VersionHandle version(String versionPath) {
        return null
    }

    void close() {
        restClient.shutdown()
    }

    def get(String path) {
        restClient.get([path: path])
    }

    def post(String path, Object body) {
        restClient.post([path: path, body: body]);
    }

    def patch(String path, Object body) {
        restClient.doRequest(new HTTPBuilder.RequestConfigDelegate([path: path, body: body], new HttpPatch(), null));
    }

    def delete(String path) {
        restClient.delete([path: path, contentType: ContentType.ANY]);
    }
}
