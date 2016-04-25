package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.impl.handle.BintrayImpl
import spock.lang.Specification

import static com.jfrog.bintray.client.test.BintraySpecSuite.*

/**
 * @author Dan Feldman
 */
class SpecialArtifactUploadSpec extends Specification {

    def DEB_REPO = "deb"
    def VAG_REPO = "vag"
    def debRepoJson
    def vagRepoJson
    BintrayImpl downloadServerClient

    def void setup() {
        debRepoJson = "{\n" +
                "  \"name\": \"" + DEB_REPO + "\",\n" +
                "  \"type\": \"debian\",\n" +
                "  \"private\": false,\n" +
                "  \"premium\": false,\n" +
                "  \"desc\": \"Deb Test Repo\"\n" +
                "}"
        vagRepoJson = "{\n" +
                "  \"name\": \"" + VAG_REPO + "\",\n" +
                "  \"type\": \"vagrant\",\n" +
                "  \"private\": false,\n" +
                "  \"premium\": false,\n" +
                "  \"desc\": \"Vag Test Repo\"\n" +
                "}"
        createRepoIfNeeded(DEB_REPO, debRepoJson)
        createRepoIfNeeded(VAG_REPO, vagRepoJson)
        downloadServerClient = createClient(getDownloadUrl())
    }

    def 'Upload Debian'() {
        setup:
        def DEB_PKG = "deb-test"
        PackageDetails debPkg = new PackageDetails(DEB_PKG).description('deb test')
                .licenses(['MIT']).vcsUrl("https://github.com/bintray/bintray-client-java.git")
        def pkg = bintray.subject(connectionProperties.username).repository(DEB_REPO).createPkg(debPkg)
        VersionDetails debVer = new VersionDetails(VERSION).description('versionDesc')
        def ver = pkg.createVersion(debVer)
        def planckDb = this.class.getResourceAsStream('/planckdb_amd64.deb')
        def planckDbPath = "pool/main/p/planckdb_amd64.deb"
        def planckDbIndexPath = "distributions/trusty/main/amd64/Packages"

        when:
        ver.uploadDebian(planckDbPath, "trusty", "main", "amd64", planckDb)
        ver.publish()
        sleep(30000)

        then:
        downloadServerClient.head("/" + connectionProperties.username + "/" + DEB_REPO + "/" + planckDbPath, null).getStatusLine().getStatusCode() == 200
//        downloadServerClient.head("/" + connectionProperties.username + "/" + DEB_REPO + "/" + planckDbIndexPath, null).getStatusLine().getStatusCode() == 200
    }

    def 'Upload Vagrant'() {
        setup:
        def VAG_PKG = "vag-test"
        PackageDetails vagPkg = new PackageDetails(VAG_PKG).description('vag test')
                .licenses(['MIT']).vcsUrl("https://github.com/bintray/bintray-client-java.git")
        def pkg = bintray.subject(connectionProperties.username).repository(VAG_REPO).createPkg(vagPkg)
        VersionDetails debVer = new VersionDetails(VERSION).description('versionDesc')
        def ver = pkg.createVersion(debVer)
        def trustyBox = this.class.getResourceAsStream('/trusty64.box')
        def trustyBoxPath = "test/trusty64_1.0.0_virtualbox.box.deb"

        when:
        ver.uploadVagrant(trustyBoxPath, "art-provider", trustyBox)
        ver.publish()
        sleep(5000)

        then:
        downloadServerClient.head("/" + connectionProperties.username + "/" + VAG_REPO + "/" + trustyBoxPath, null).getStatusLine().getStatusCode() == 200
    }

    def cleanup() {
        deleteRepo(DEB_REPO)
        deleteRepo(VAG_REPO)
    }
}