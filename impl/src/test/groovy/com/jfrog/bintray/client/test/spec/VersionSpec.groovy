package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.details.Attribute
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.VersionHandle
import com.jfrog.bintray.client.api.model.Version
import com.jfrog.bintray.client.impl.model.VersionImpl
import com.jfrog.bintray.client.test.BintraySpecSuite
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.codehaus.jackson.map.ObjectMapper
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification

import static com.jfrog.bintray.client.test.BintraySpecSuite.*
import static org.apache.http.HttpStatus.SC_NOT_FOUND

/**
 * @author Dan Feldman
 */
class VersionSpec extends Specification {

    def 'Version created'() {
        setup:
        def pkg = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder)

        when:
        Version version = pkg.createVersion(versionBuilder).get()
        JsonSlurper slurper = new JsonSlurper()
        def actual = slurper.parseText(IOUtils.toString(restClient.get("/packages/$connectionProperties.username/$REPO_NAME/$PKG_NAME/versions/$VERSION", null).getEntity().getContent()))

        then:
        version.name() == actual.name
        version.description() == actual.desc
        version.pkg() == actual.package
        version.repository() == actual.repo
        version.owner() == actual.owner
        version.labels() == actual.labels
        version.vcsTag() == actual.vcs_tag
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

    def 'version exists'() {
        when:
        // Create the version:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)

        then:
        // Check that the version exists:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).version(VERSION).exists()

        when:
        // Delete the version:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).version(VERSION).delete()

        then:
        // Check that the package does not exist:
        !bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).version(VERSION).exists()
    }

    def 'attributes set on version'() {
        setup:
        def ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)

        when:
        ver.setAttributes(attributes)

        JsonSlurper slurper = new JsonSlurper()
        def actualVersion = slurper.parseText(IOUtils.toString(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/versions/" + VERSION, null).getEntity().getContent()))
        def actualAttributes = slurper.parseText(IOUtils.toString(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/versions/" + VERSION + "/attributes", null).getEntity().getContent()))

        then:
        ['a', 'b', 'c'] == actualVersion.attribute_names.sort()
        and:
        expectedAttributes == actualAttributes.sort().toString()

    }

    def 'wrong version gives 404'() {
        when:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).version('3434').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'Version Creation using VersionDetails'() {
        setup:
        ObjectMapper mapper = new ObjectMapper()
        VersionDetails verDetailsFromJson = mapper.readValue(verJson, VersionDetails.class)
        PackageDetails pkgDetailsFromJson = mapper.readValue(pkgJson, PackageDetails.class)


        List<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute<String>("verAtt1", Attribute.Type.string, "verVal1"))
        attrs.add(new Attribute<Double>("verAtt3", Attribute.Type.number, 1, 8.2, 6))
        attrs.add(new Attribute<DateTime>("verAtt5", Attribute.Type.date, DateTime.parse("2014-01-01T17:36:37+0100")))

        VersionDetails verDetails = new VersionDetails(tempVerName).description("Version Test").releaseNotesFile("README.md")
                .released(DateTime.parse("2014-01-01T17:36:37+0100")).useTagReleaseNotes(false).vcsTag("3.8")

        verDetails.setOrdinal(5)
        verDetails.setAttributes(attrs)
        verDetails.setLabels(["cool", "awesome", "gorilla"])
        verDetails.setPkg(tempPkgName)
        verDetails.setRepo("generic")
        verDetails.setOwner(bintray.subject(connectionProperties.username).name())

        VersionImpl locallyCreated = new VersionImpl(verDetails);

        when:
        JsonSlurper slurper = new JsonSlurper()
        VersionHandle verHandle = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgDetailsFromJson).createVersion(verDetailsFromJson)
        VersionImpl ver = verHandle.get()
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName + "/versions/" + tempVerName, null).getEntity().getContent()))
        List<Attribute> attributes = Attribute.getAttributeListFromJson(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName + "/versions/" + tempVerName + "/attributes", null).getEntity().getContent())

        then:
        //PackageImpl
        locallyCreated.equals(ver)

        //jsons
        verDetailsFromJson.getName().equals(directJson.name)
        verDetailsFromJson.getDescription().equals(directJson.desc)
        verDetailsFromJson.getRepo().equals(directJson.repo)
        verDetailsFromJson.getPkg().equals(directJson.package)
        verDetailsFromJson.getOwner().equals(directJson.owner)
        for (int i = 0; i < verDetailsFromJson.getLabels().size(); i++) {
            verDetailsFromJson.getLabels().sort().get(i).equalsIgnoreCase(directJson.labels.sort()[i])
        }
        for (int i = 0; i < verDetailsFromJson.getAttributeNames().size(); i++) {
            verDetailsFromJson.getAttributeNames().sort().get(i).equalsIgnoreCase(directJson.attribute_names.sort()[i])
        }

        directJson.released != null
        verDetailsFromJson.getUseTagReleaseNotes().equals(directJson.github_use_tag_release_notes)
        verDetailsFromJson.getVcsTag().equals(directJson.vcs_tag)

        //Attributes
        for (Attribute attr : attributes) {
            attr.equals(attrs.get(0)) || attr.equals(attrs.get(1)) || attr.equals(attrs.get(2))
        }

        cleanup:
        try {
            String cleanPkg = "/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName
            restClient.delete(cleanPkg, null)
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
    }

    def cleanup() {
        BintraySpecSuite.cleanup()
    }
}