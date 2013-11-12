package com.jfrog.bintray.client.impl

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
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Shared
import spock.lang.Specification

import static java.lang.System.getenv
import static java.lang.System.setProperty
import static org.apache.http.HttpStatus.SC_NOT_FOUND
/**
 * @author Noam Y. Tenne
 */
class BintrayClientSpec extends Specification {
    private final static String REPO_NAME = 'generic'
    private final static String PKG_NAME = 'bla'
    private static String VERSION = '1.0'
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
        restClient.contentEncoding = ContentEncoding.Type.GZIP
        restClient.auth.basic connectionProperties.username as String, connectionProperties.apiKey as String
        pkgBuilder = new PackageDetails(PKG_NAME).description('blabla').labels(['l1', 'l2']).licenses(['Apache 2'])
        versionBuilder = new VersionDetails(VERSION).description('versionDesc')
        setProperty 'org.apache.commons.logging.Log', 'org.apache.commons.logging.impl.SimpleLog'
        setProperty 'org.apache.commons.logging.simplelog.showdatetime', 'true'
        setProperty 'org.apache.commons.logging.simplelog.log.org.apache.http', 'DEBUG'
        setProperty 'org.apache.commons.logging.simplelog.log.org.apache.http.wire', 'ERROR'

    }

    def 'Connection is successful and subject has correct username and avatar'() {
        //noinspection JavaStylePropertiesInvocation,GroovySetterCallCanBePropertyAccess
        setup:
        //setter returns `this`? WTF!
        Gravatar gravatar = new Gravatar().setSize(140)

        when:
        Subject clienttests = bintray.currentSubject().get()

        then:
        clienttests.name == connectionProperties.username
        new URL(clienttests.gravatarId).bytes == gravatar.download(connectionProperties.email as String)
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
        def repository = bintray.currentSubject().repository(REPO_NAME)

        when:
        Pkg pkg = repository.createPkg(pkgBuilder).get()
        def actual = restClient.get(path: "/packages/$connectionProperties.username/$REPO_NAME/$PKG_NAME").data

        then:
        pkg.name() == actual.name
        pkg.repository() == actual.repository
        pkg.owner() == actual.owner
        pkg.description() == actual.desc
        pkg.labels() == actual.labels
        pkg.attributeNames() == actual.attribute_names
        println pkg
        pkg.rating() == actual.rating?.toInteger()
        pkg.ratingCount() == actual.rating_count?.toInteger()
        pkg.followersCount() == actual.followers_count?.toInteger()
        pkg.created() == ISODateTimeFormat.dateTime().parseDateTime(actual.created as String)
        pkg.versions() == actual.versions
        pkg.latestVersion() == actual.latest_version
        pkg.updated() == ISODateTimeFormat.dateTime().parseDateTime(actual.updated as String)
        pkg.linkedToRepo() == actual.linked_to_repo
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

    def '404s'() {
        when:
        bintray.subject('bla').get()
        then:
        HttpResponseException e = thrown()
        e.statusCode == SC_NOT_FOUND
        and:
        when:
        bintray.currentSubject().repository('bla').get()
        then:
        e = thrown()
        e.statusCode == SC_NOT_FOUND
        and:
        when:
        bintray.currentSubject().repository(REPO_NAME).pkg('bla').get()
        then:
        e = thrown()
        e.statusCode == SC_NOT_FOUND
        and:
        when:
        bintray.currentSubject().repository(REPO_NAME).pkg(PKG_NAME).version('3434').get()
        then:
        e = thrown()
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