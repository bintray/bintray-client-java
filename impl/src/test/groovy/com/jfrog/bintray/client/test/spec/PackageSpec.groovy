package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.details.Attribute
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.impl.model.PackageImpl
import com.jfrog.bintray.client.test.BintraySpecSuite
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.apache.http.HttpHeaders
import org.codehaus.jackson.map.ObjectMapper
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_CONTENT
import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PKGS
import static com.jfrog.bintray.client.test.BintraySpecSuite.*
import static org.apache.http.HttpStatus.SC_NOT_FOUND

/**
 * @author Dan Feldman
 */
class PackageSpec extends Specification {

    def 'Package created'() {
        setup:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)

        Map<String, String> headers = new HashMap<>();
        String auth = (connectionProperties.username + ":" + connectionProperties.apiKey)
        headers.put(HttpHeaders.AUTHORIZATION, "Basic " + auth.bytes.encodeBase64())
        String path = "/" + API_CONTENT + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/" + VERSION + "/com/jfrog/bintray/bintray-test/1.0/bintray-test-1.0.pom;publish=1"
        restClient.putBinary(path, headers, new ByteArrayInputStream('bla'.bytes))

        when:
        Pkg pkg = bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).get()
        JsonSlurper slurper = new JsonSlurper()
        def actual = slurper.parseText(IOUtils.toString(restClient.get("/packages/$connectionProperties.username/$REPO_NAME/$PKG_NAME", null).entity.content))

        then:
        pkg.name() == actual.name
        pkg.repository() == actual.repo
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
        pkg.linkedToRepos() == actual.linked_to_repos
        pkg.systemIds() == actual.system_ids
    }

    def 'package exists'() {
        when:
        // Create the package:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder)

        then:
        // Check that the package exists:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).exists()

        when:
        // Delete the package:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).delete()

        then:
        // Check that the package does not exist:
        !bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).exists()
    }

    def 'attributes set on package'() {
        setup:
        def pkg = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder)

        when:
        pkg.setAttributes(attributes)

        JsonSlurper slurper = new JsonSlurper()
        def actualPackage = slurper.parseText(IOUtils.toString(restClient.get("/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME, null).getEntity().getContent()))
        def actualAttributes = slurper.parseText(IOUtils.toString(restClient.get("/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/attributes", null).getEntity().getContent()))

        then:
        ['a', 'b', 'c'] == actualPackage.attribute_names.sort()
        and:
        expectedPkgAttributes.equalsIgnoreCase(actualAttributes.sort().toString())
    }

    def 'wrong package gives 404'() {
        when:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'Package Creation Using PackageDetails'() {
        setup:
        ObjectMapper mapper = new ObjectMapper();
        PackageDetails pkgDetailsFromJson = mapper.readValue(pkgJson, PackageDetails.class)

        List<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute<String>("att1", Attribute.Type.string, "val1"))
        attrs.add(new Attribute<Double>("att3", Attribute.Type.number, 1, 3.3, 5))
        attrs.add(new Attribute<DateTime>("att5", Attribute.Type.date, DateTime.parse("2011-07-14T19:43:37+0100")))

        PackageDetails pkgDetails = new PackageDetails(tempPkgName).description("Bintray Client Java").websiteUrl("http://www.jfrog.com")
                .issueTrackerUrl("https://github.com/bintray/bintray-client-java/issues").vcsUrl("https://github.com/bintray/bintray-client-java.git")
                .licenses(["MIT"]).labels(["cool", "awesome", "gorilla"]).publicDownloadNumbers(false).attributes(attrs)
        pkgDetails.setRepo("generic")
        pkgDetails.setOwner(bintray.subject(connectionProperties.username).name())
        PackageImpl locallyCreated = new PackageImpl(pkgDetails);

        when:
        JsonSlurper slurper = new JsonSlurper()
        PackageHandle pkgHandle = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgDetailsFromJson)
        PackageImpl pkg = pkgHandle.get()
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName, null).getEntity().getContent()))
        List<Attribute> attributes = Attribute.getAttributeListFromJson(restClient.get("/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName + "/attributes", null).getEntity().getContent())

        then:
        //PackageImpl
        locallyCreated.equals(pkg)

        //jsons
        pkgDetailsFromJson.getName().equals(directJson.name)
        pkgDetailsFromJson.getRepo().equals(directJson.repo)
        pkgDetailsFromJson.getOwner().equals(directJson.owner)
        pkgDetailsFromJson.getDescription().equals(directJson.desc)
        pkgDetailsFromJson.getWebsiteUrl().equals(directJson.website_url)
        pkgDetailsFromJson.getIssueTrackerUrl().equals(directJson.issue_tracker_url)
        pkgDetailsFromJson.getVcsUrl().equals(directJson.vcs_url)
        for (int i = 0; i < pkgDetails.getLabels().size(); i++) {
            pkgDetails.getLabels().sort().get(i).equalsIgnoreCase(directJson.labels.sort()[i])
        }
        pkgDetailsFromJson.getPublicDownloadNumbers().equals(directJson.public_download_numbers)

        //Attributes
        for (Attribute attr : attributes) {
            attr.equals(attrs.get(0)) || attr.equals(attrs.get(1)) || attr.equals(attrs.get(2))
        }

        cleanup:
        try {
            String cleanPkg = "/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName
            restClient.delete(cleanPkg, null)
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
    }

    def cleanup() {
        BintraySpecSuite.cleanup()
    }
}