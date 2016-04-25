package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.ObjectMapperHelper
import com.jfrog.bintray.client.api.details.Attribute
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.Bintray
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.handle.VersionHandle
import com.jfrog.bintray.client.impl.BintrayClient
import com.jfrog.bintray.client.impl.HttpClientConfigurator
import com.jfrog.bintray.client.impl.handle.BintrayImpl
import com.jfrog.bintray.client.impl.model.PackageImpl
import com.jfrog.bintray.client.impl.model.VersionImpl
import org.apache.commons.io.IOUtils
import org.codehaus.jackson.map.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PKGS
import static com.jfrog.bintray.client.test.BintraySpecSuite.*
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_OK

/**
 * @author Noam Y. Tenne
 * @author Dan Feldman
 *
 */
class BintrayClientSpec extends Specification {

    @Shared
    private static Map files = ['com/bla/bintray-client-java-api.jar'        : getClass().getResourceAsStream('/testJar1.jar'),
                                'org/foo/bar/bintray-client-java-service.jar': getClass().getResourceAsStream('/testJar2.jar')]

    def void setup() {
        createRepoIfNeeded(REPO_NAME, genericRepoJson)
    }

    def 'Test correct URL encoding'() {
        setup:
        def path1 = "content/user/" + REPO_NAME + "/" + PKG_NAME + "/" + VERSION + "/com/jfrog/bintray/bintray-test/1.0/bintray-test-1.0.pom;publish=1"
        def path2 = "docker/bla/dockertest/v1/repositories/library/ubuntu"
        def path3 = "docker/bla/dockertest/v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json with space.ext"
        def path4 = "bla/someUser/test?a=b&c=d"
        def path5 = "bla/someUser/testMatrix;a+=b"
        def path6 = "t%st/spe^al/ch&ar\$/*()!#/ok?"

        when:
        def encodedPath1 = ((BintrayImpl) bintray).createUrl(path1)
        def encodedPath2 = ((BintrayImpl) bintray).createUrl(path2)
        def encodedPath3 = ((BintrayImpl) bintray).createUrl(path3)
        def encodedPath4 = ((BintrayImpl) bintray).createUrl(path4)
        def encodedPath5 = ((BintrayImpl) bintray).createUrl(path5)
        def encodedPath6 = ((BintrayImpl) bintray).createUrl(path6)

        then:
        String url = getApiUrl()
        encodedPath1.toString().equals(url + "/content/user/generic/bla/1.0/com/jfrog/bintray/bintray-test/1.0/bintray-test-1.0.pom;publish=1")
        encodedPath2.toString().equals(url + "/docker/bla/dockertest/v1/repositories/library/ubuntu")
        encodedPath3.toString().equals(url + "/docker/bla/dockertest/v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json%20with%20space.ext")
        encodedPath4.toString().equals(url + "/bla/someUser/test?a=b&c=d")
        encodedPath5.toString().equals(url + "/bla/someUser/testMatrix;a+=b")
        encodedPath6.toString().equals(url + "/t%25st/spe%5Eal/ch&ar\$/*()!%23/ok?")
    }

    def 'on error response is returned without parsing'() {
        setup:
        Bintray wrongBintray = BintrayClient.create(getApiUrl(),
            connectionProperties.username as String,
            connectionProperties.apiKey as String)
        when:
        wrongBintray.subject('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
        e.reason == 'Not Found'
    }

    def 'wrong subject gives 404'() {
        when:
        bintray.subject('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'files uploaded and can be accessed by the author'() {
        setup:
        def ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)
        def downloadServerClient = createClient(getDownloadUrl())
        files = ['com/bla/bintray-client-java-api.jar'        : getClass().getResourceAsStream('/testJar1.jar'),
                 'org/foo/bar/bintray-client-java-service.jar': getClass().getResourceAsStream('/testJar2.jar')]

        when:
        ver.upload(files)
        sleep(4000)
        def get1 = downloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(0), null)
        def get2 = downloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(1), null)

        then:
        get1.getStatusLine().getStatusCode() == SC_OK
        and:
        get2.getStatusLine().getStatusCode() == SC_OK
    }

    def 'unpublished files can\'t be seen by anonymous'() {
        setup:
        sleep(15000) //wait for previous deletions to propagate
        def ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)
        HttpClientConfigurator conf = new HttpClientConfigurator();
        String url = getDownloadUrl()
        def anonymousDownloadServerClient = new BintrayImpl(conf.hostFromUrl(url).noRetry().noCookies().getClient(), url, 5, 90000)
        files = ['com/bla/bintray-client-java-api.jar'        : getClass().getResourceAsStream('/testJar1.jar'),
                 'org/foo/bar/bintray-client-java-service.jar': getClass().getResourceAsStream('/testJar2.jar')]

        when:
        sleep(10000)
        ver.upload(files)
        sleep(20000)
        anonymousDownloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(0), null)

        then:
        BintrayCallException bce = thrown()
        bce.getStatusCode().equals(401)
    }

    def 'publish artifacts'() {
        setup:
        files = ['com/bla/bintray-client-java-api.jar'        : getClass().getResourceAsStream('/testJar1.jar'),
                 'org/foo/bar/bintray-client-java-service.jar': getClass().getResourceAsStream('/testJar2.jar')]
        VersionHandle ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder).upload(files)
        HttpClientConfigurator conf = new HttpClientConfigurator();

        String url = getDownloadUrl()
        def anonymousDownloadServerClient = new BintrayImpl(conf.hostFromUrl(url).noRetry().getClient(), url, 5, 90000)

        when:
        sleep(5000)
        ver.publish()
        sleep(20000)
        def response = anonymousDownloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(0), null)

        then:
        response.getStatusLine().getStatusCode() == SC_OK
    }

    def 'sync-publish artifacts'() {
        setup:
        files = ['com/bla/bintray-client-java-api.jar'        : getClass().getResourceAsStream('/testJar1.jar'),
                 'org/foo/bar/bintray-client-java-service.jar': getClass().getResourceAsStream('/testJar2.jar')]
        VersionHandle ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder).upload(files)
        HttpClientConfigurator conf = new HttpClientConfigurator();

        String url = getDownloadUrl()
        def anonymousDownloadServerClient = new BintrayImpl(conf.hostFromUrl(url).noRetry().getClient(), url, 5, 90000)

        when:
        sleep(5000)
        ver.publishSync()
        sleep(20000)
        def response = anonymousDownloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(0), null)

        then:
        response.getStatusLine().getStatusCode() == SC_OK
    }

    def 'discard artifacts'() {
        setup:
        files = ['com/bla/bintray-client-java-api.jar'        : getClass().getResourceAsStream('/testJar1.jar'),
                 'org/foo/bar/bintray-client-java-service.jar': getClass().getResourceAsStream('/testJar2.jar')]
        VersionHandle ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder).upload(files)

        when:
        sleep(4000)
        ver.discard()
        sleep(4000) //wait for propagation to dl and stuff

        String url = getDownloadUrl()
        "$url/$connectionProperties.username/$REPO_NAME/${files.keySet().asList().get(0)}".toURL().content

        then:
        IOException ioe = thrown()
        ioe.getMessage().contains("401") || ioe instanceof FileNotFoundException
    }

    def 'Attribute Serialization and Deserialization'() {
        setup:
        String attrs = assortedAttributes

        when:
        List<Attribute> attributes = Attribute.getAttributeListFromJson(IOUtils.toInputStream(attrs))
        String out = Attribute.getJsonFromAttributeList(attributes)

        then:
        attributes.size == 3
        attributes.get(0).getName().equals("verAttr2")
        attributes.get(0).getType().equals(Attribute.Type.string)
        attributes.get(0).getValues() == ["val1", "val2"]
        out.equals(attrs)
    }

    def 'send empty values - verify they are not null'() {

        String minimalPkgName = "MyPackage"

        String minimalPkgJson = "{\n" +
                "\t\t\"name\": \"" + minimalPkgName + "\",\n" +
                "\t\t\"repo\": \"" + REPO_NAME + "\",\n" +
                "\t\t\"owner\": \"" + connectionProperties.username + "\",\n" +
                "\t\t\"desc\": \"\",\n" +
                "\t\t\"website_url\": \"\",\n" +
                "\t\t\"labels\": [],\n" +
                "\t\t\"vcs_url\": \"https://github.com/bintray/bintray-client-java.git\",\n" +
                "\t\t\"licenses\": [\"MIT\"]\n" +
                "}"

        String minimalVerJson = "{\n" +
                "\t\t\"name\": \"3.3.3\",\n" +
                "\t\t\"vcs_tag\": \"\",\n" +
                "\t\t\"labels\": null,\n" +
                "\t\t\"description\": \"\"\n" +
                "}"

        setup:
        ObjectMapper mapper = ObjectMapperHelper.get()

        ArrayList<String> licenses = new ArrayList<>();
        licenses.add("Apache-2.0")
        PackageDetails newPkgDetails = new PackageDetails(minimalPkgName);
        newPkgDetails.licenses(licenses);
        newPkgDetails.setRepo(REPO_NAME)
        newPkgDetails.setSubject(connectionProperties.username)
        newPkgDetails.setVcsUrl("https://github.com/bintray/bintray-client-java.git")
        newPkgDetails.setDescription("")

        VersionDetails newVerDetails = new VersionDetails("2.2.0")
        newVerDetails.setDescription("")

        PackageDetails pkgDetailsFromJson = mapper.readValue(minimalPkgJson, PackageDetails.class)
        VersionDetails verDetailsFromJson = mapper.readValue(minimalVerJson, VersionDetails.class)

        when:
        RepositoryHandle repo = bintray.subject(connectionProperties.username).repository(REPO_NAME)

        PackageHandle pkg = repo.createPkg(newPkgDetails)
        VersionHandle ver = pkg.createVersion(newVerDetails)

        pkg.update(pkgDetailsFromJson)
        ver.update(verDetailsFromJson)

        String pkgJsonContent = PackageImpl.getCreateUpdateJson(pkgDetailsFromJson);
        String verJsonContent = VersionImpl.getCreateUpdateJson(verDetailsFromJson);

        then:
        pkgJsonContent.equals("{\"name\":\"MyPackage\",\"labels\":[],\"licenses\":[\"MIT\"],\"desc\":\"\",\"website_url\":\"\",\"vcs_url\":\"https://github.com/bintray/bintray-client-java.git\"}")
        verJsonContent.contentEquals("{\"name\":\"3.3.3\",\"vcs_tag\":\"\"}")

        cleanup:
        try {
            String cleanPkg = "/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + minimalPkgName
            restClient.delete(cleanPkg, null)
        } catch (BintrayCallException bce) {
            if (bce.getStatusCode() != 404) {
                System.err.println("cleanup: " + bce)
            }
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
    }

    def cleanup() {
        deleteRepo(REPO_NAME)
    }
}