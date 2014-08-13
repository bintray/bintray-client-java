package com.jfrog.bintray.client.impl

import com.jfrog.bintray.client.BintrayCallException
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.Bintray
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.api.model.Subject
import com.jfrog.bintray.client.api.model.Version
import com.timgroup.jgravatar.Gravatar
import groovyx.net.http.ContentEncoding
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import groovyx.net.http.ResponseParseException
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Shared
import spock.lang.Specification

import static groovyx.net.http.ContentType.BINARY
import static groovyx.net.http.ContentType.JSON
import static java.lang.System.getenv
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.auth.params.AuthPNames.TARGET_AUTH_PREF
import static org.apache.http.client.params.AuthPolicy.BASIC
import static org.apache.http.client.params.CookiePolicy.IGNORE_COOKIES
import static org.apache.http.client.params.HttpClientParams.setCookiePolicy

/**
 * @author Noam Y. Tenne
 */
class BintrayClientSpec extends Specification {
    private final static String REPO_NAME = 'maven'
    private final static String PKG_NAME = 'bla'
    private final static String VERSION = '1.0'
    public static final String ATTRIBUTE_NAME = 'att1'
    public static final String ATTRIBUTE_VALUE = 'bla'
    @Shared
    private Properties connectionProperties
    @Shared
    private Bintray bintray
    @Shared
    private RESTClient restClient
    @Shared
    private PackageDetails pkgBuilder
    @Shared
    private VersionDetails versionBuilder

    void setup() {
    }

    def setupSpec() {
        this.connectionProperties = new Properties()
        def streamFromProperties = this.class.getResourceAsStream('/bintray-client.properties')
        if (streamFromProperties) {
            streamFromProperties.withStream {
                this.connectionProperties.load(it)
            }
        }
        def usernameFromEnv = getenv('BINTRAY_USERNAME')
        if (usernameFromEnv) {
            connectionProperties.username = usernameFromEnv
        }
        def apiKeyFromEnv = getenv('BINTRAY_API_KEY')
        if (apiKeyFromEnv) {
            connectionProperties.apiKey = apiKeyFromEnv
        }
        def emailFromEnv = getenv('BINTRAY_EMAIL')
        if (emailFromEnv) {
            connectionProperties.email = emailFromEnv
        }
        assert this.connectionProperties
        assert this.connectionProperties.username
        assert this.connectionProperties.apiKey
        assert this.connectionProperties.email
        bintray = BintrayClient.create(this.connectionProperties.url as String ?: 'https://api.bintray.com', this.connectionProperties.username as String, this.connectionProperties.apiKey as String)
        restClient = new RESTClient('https://api.bintray.com')
        def params = restClient.client.getParams()
        setCookiePolicy params, IGNORE_COOKIES
        params.setParameter(TARGET_AUTH_PREF, [BASIC])
        restClient.contentEncoding = ContentEncoding.Type.GZIP
        restClient.auth.basic connectionProperties.username as String, connectionProperties.apiKey as String
        pkgBuilder = new PackageDetails(PKG_NAME).description('bla-bla').labels(['l1', 'l2']).licenses(['Apache-2.0'])
        versionBuilder = new VersionDetails(VERSION).description('versionDesc')
    }

    def 'Connection is successful and subject has correct username and avatar'() {
        //noinspection JavaStylePropertiesInvocation,GroovySetterCallCanBePropertyAccess
        setup:
        //setter returns `this`? WTF!
        Gravatar gravatar = new Gravatar().setSize(140)

        when:
        Subject clientTests = bintray.currentSubject().get()

        then:
        clientTests.name == connectionProperties.username
        new URL(clientTests.gravatarId).bytes == gravatar.download(connectionProperties.email as String)
    }

    def 'Default Repos exist'(String repoName, def _) {
        expect:
        bintray.currentSubject().repository(repoName)

        where:
        repoName  | _
        'maven'   | _
        'rpm'     | _
        'deb'     | _
        'generic' | _
    }

    def 'Package created'() {
        setup:
        bintray.currentSubject().repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)

        try {
            restClient.put path: "/content/$connectionProperties.username/$REPO_NAME/$PKG_NAME/$VERSION/com/jfrog/bintray/bintray-test/1.0/bintray-test-1.0.pom;publish=1",
                    body: new ByteArrayInputStream('bla'.bytes), requestContentType: BINARY, headers: [Authorization: "Basic ${"$connectionProperties.username:$connectionProperties.apiKey".toString().bytes.encodeBase64()}"]
        } catch (ResponseParseException e) {
            //Bintray returns null with content type JSON, restClient fails on parsing
            assert e.message == 'Created'
        }
        when:
        Pkg pkg = bintray.currentSubject().repository(REPO_NAME).pkg(PKG_NAME).get()
        def actual = restClient.get(path: "/packages/$connectionProperties.username/$REPO_NAME/$PKG_NAME").data

        then:
        pkg.name() == actual.name
        pkg.repository() == actual.repository
        pkg.owner() == actual.owner
        pkg.description() == actual.desc
        pkg.labels() == actual.labels
        pkg.attributeNames() == actual.attribute_names
        pkg.rating() == actual.rating?.toInteger()
        pkg.ratingCount() == actual.rating_count?.toInteger()
        pkg.followersCount() == actual.followers_count?.toInteger()
        pkg.created() == ISODateTimeFormat.dateTime().parseDateTime(actual.created as String)
        pkg.versions() == actual.versions
        pkg.latestVersion() == actual.latest_version
        pkg.updated() == ISODateTimeFormat.dateTime().parseDateTime(actual.updated as String)
        pkg.linkedToRepo() == actual.linked_to_repo
        pkg.systemIds() == actual.system_ids
    }

    def 'Version created'() {
        setup:
        def pkg = bintray.currentSubject().repository(REPO_NAME).createPkg(pkgBuilder)

        when:
        Version version = pkg.createVersion(versionBuilder).get()
        def actual = restClient.get(path: "/packages/$connectionProperties.username/$REPO_NAME/$PKG_NAME/versions/$VERSION").data

        then:
        version.name() == actual.name
        version.description() == actual.desc
        version.pkg() == actual.pkg
        version.repository() == actual.repository
        version.owner() == actual.owner
        version.labels() == actual.labels
        version.attributeNames() == actual.attribute_names
        version.ordinal() == actual.ordinal.toInteger()
        if (actual.created) {
            version.created() == ISODateTimeFormat.dateTime().parseDateTime(actual.created as String)
        }
        if (actual.updated) {
            version.updated() == ISODateTimeFormat.dateTime().parseDateTime(actual.updated as String)
        }
        if (actual.released) {
            version.released() == ISODateTimeFormat.dateTime().parseDateTime(actual.released as String)
        }
    }

    def 'search by attributes'() {
        setup:
        def repo = bintray.currentSubject().repository(REPO_NAME)

        repo.createPkg(pkgBuilder)
        def attributesQuery = "[{\"name\": \"$ATTRIBUTE_NAME\", \"values\" : [\"$ATTRIBUTE_VALUE\"], \"type\": \"string\"}]"
        restClient.post(path: "/packages/$connectionProperties.username/$REPO_NAME/$PKG_NAME/attributes",
                contentType: JSON,
                body: attributesQuery)

        when:
        /* the following code is analogous to this rest query payload:
        [
        {"att1" : ["val1", "val2"]}, //att1 value is either val1 or val2 (att1 is a scalar)
        {"att2": "[1,3]"}, //att2 value is equal to or greater than 1 and equal to or smaller than 3
        {"att3": "[,3]"}, //att3 value is equals to or smaller than 3
        {"att4": "[,3["}, //att3 value is smaller than 3
        {"att5": "]2011-07-14T19:43:37+0100,]"}, //att5 value  is after 2011-07-14T19:43:37+0100 (dates are defined in ISO8601 format)
        ]
         */
//        def results = repo.searchForPackage().byAttributeName('att1').in('val1', 'val2').and().
//                byAttributeName('att2').greaterOrEqualsTo(1).lessOrEquals(3).and().
//                byAttributeName('att3').lessOrEquals(3).and().
//                byAttributeName('att4').lessThan(3).and().
//                byAttributeName('att5').after(new DateTime(2011, 7, 14, 19, 43, 37, DateTimeZone.forOffsetHours(1))).and().
//                byAttributeName('att6').equals(3).search()

        List<Pkg> results = repo.searchForPackage().byAttributeName(ATTRIBUTE_NAME).equals(ATTRIBUTE_VALUE).search()

        then:
        results
        results.size() == 1
        Pkg pkg = results[0]
        pkg.name() == PKG_NAME
        pkg.attributeNames()[0] == ATTRIBUTE_NAME
    }

    def 'on error response is returned without parsing'() {
        setup:
        Bintray wrongBintray = BintrayClient.create('https://api.bintray.com/bla', this.connectionProperties.username as String, this.connectionProperties.apiKey as String)
        when:
        wrongBintray.subject('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
        e.reason == 'Not Found'
        e.message.toLowerCase().contains('<body>')
    }

    def 'wrong subject gives 404'() {
        when:
        bintray.subject('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'wrong repository gives 404'() {
        when:
        bintray.currentSubject().repository('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'wrong package gives 404'() {
        when:
        bintray.currentSubject().repository(REPO_NAME).pkg('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'wrong version gives 404'() {
        when:
        bintray.currentSubject().repository(REPO_NAME).pkg(PKG_NAME).version('3434').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def cleanup() {
        try {
            restClient.delete(path: "/packages/$connectionProperties.username/$REPO_NAME/$PKG_NAME")
        } catch (HttpResponseException e) {
            if (e.response.status != SC_NOT_FOUND) //don't care
                throw e
        }
    }
}