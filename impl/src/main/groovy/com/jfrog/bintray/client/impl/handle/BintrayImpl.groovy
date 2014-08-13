package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.BintrayCallException
import com.jfrog.bintray.client.api.handle.*
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.ParserRegistry
import groovyx.net.http.RESTClient
import org.apache.http.auth.AuthScope
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.params.CookiePolicy
import org.apache.http.client.params.HttpClientParams
import org.apache.http.impl.client.DefaultHttpClient

import static org.apache.http.client.params.HttpClientParams.setCookiePolicy

/**
 * @author Noam Y. Tenne
 */
class BintrayImpl implements Bintray {

    private final RESTClient restClient

    BintrayImpl(RESTClient restClient) {
        this.restClient = restClient
        restClient.handler.failure = {HttpResponseDecorator resp ->
            InputStreamReader reader = new InputStreamReader(resp.getEntity().getContent(),
                    ParserRegistry.getCharset(resp))
            throw new BintrayCallException(reader.text, resp.statusLine.statusCode, resp.statusLine.reasonPhrase)
        }
    }

    String uri() {
        restClient.uri.toString()
    }

    SubjectHandle currentSubject() {
        def credentials = restClient.auth.builder.client.credentialsProvider.getCredentials(AuthScope.ANY)
        if(!credentials){
            throw new IllegalStateException('Can\'t determine current user, did you use BintrayClient.create() without parameters?')
        }
        String authenticatingSubject =
                credentials.userPrincipal.name
        subject(authenticatingSubject)
    }

    SubjectHandle subject(String subject) {
        new SubjectHandleImpl(this, subject)
    }

    RepositoryHandle repository(String repositoryPath) {
        throw new UnsupportedOperationException('TODO implement full path resolution that receives "/subject/repo/"')
    }

    PackageHandle pkg(String packagePath) {
        throw new UnsupportedOperationException('TODO implement full path resolution that receives "/subject/repo/pkg"')
    }

    VersionHandle version(String versionPath) {
        throw new UnsupportedOperationException('TODO implement full path resolution that receives "/subject/repo/pkg/version"')
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

    @SuppressWarnings("GroovyAccessibility")
    def patch(String path, Object body) {
        restClient.doRequest(new HTTPBuilder.RequestConfigDelegate([path: path, body: body], new HttpPatch(), null));
    }

    def delete(String path) {
        restClient.delete([path: path, contentType: ContentType.ANY]);
    }
}
