package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.details.RepositoryDetails
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.api.model.Subject
import com.jfrog.bintray.client.impl.model.RepositoryImpl
import com.jfrog.bintray.client.test.BintraySpecSuite
import com.timgroup.jgravatar.Gravatar
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType
import org.codehaus.jackson.map.ObjectMapper
import spock.lang.Specification

import static com.jfrog.bintray.client.test.BintraySpecSuite.*
import static org.apache.http.HttpStatus.SC_NOT_FOUND

/**
 * @author Dan Feldman
 */
class RepoSpec extends Specification {

    def 'Connection is successful and subject has correct username and avatar'() {
        //noinspection JavaStylePropertiesInvocation,GroovySetterCallCanBePropertyAccess
        setup:
        Gravatar gravatar = new Gravatar().setSize(140)

        when:
        Subject clientTests = bintray.subject(connectionProperties.username).get()

        then:
        clientTests.name == connectionProperties.username
        new URL(clientTests.gravatarId).bytes == gravatar.download(connectionProperties.email as String)
    }

    def 'Default Repos exist'(String repoName, def _) {
        expect:
        bintray.subject(connectionProperties.username).repository(repoName)

        where:
        repoName  | _
        'maven'   | _
        'rpm'     | _
        'deb'     | _
        'generic' | _
    }

    def 'search by attributes'() {
        setup:
        def repo = bintray.subject(connectionProperties.username).repository(REPO_NAME)

        repo.createPkg(pkgBuilder)
        String attributesQuery = "[{\"name\": \"" + ATTRIBUTE_NAME + "\", \"values\" : [\"" + ATTRIBUTE_VALUE + "\"], \"type\": \"string\"}]"
        def headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        restClient.post("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/" + "attributes", headers,
                new ByteArrayInputStream(attributesQuery.getBytes()))

        when:
        List<Pkg> results = repo.searchForPackage().byAttributeName(ATTRIBUTE_NAME).equalsVal(ATTRIBUTE_VALUE).searchPackage()

        then:
        results
        results.size() == 1
        Pkg pkg = results[0]
        pkg.name() == PKG_NAME
        pkg.attributeNames()[0] == ATTRIBUTE_NAME
    }

    def 'wrong repository gives 404'() {
        when:
        bintray.subject(connectionProperties.username).repository('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'Repo creation using RepositoryDetails'() {
        setup:
        ObjectMapper mapper = new ObjectMapper()
        RepositoryDetails repositoryDetails = mapper.readValue(repoJson, RepositoryDetails.class)

        when:
        JsonSlurper slurper = new JsonSlurper()
        RepositoryHandle repoHandle = bintray.subject(connectionProperties.username).createRepo(repositoryDetails)
        RepositoryImpl locallyCreated = new RepositoryImpl(repositoryDetails);

        RepositoryImpl repo = repoHandle.get()
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/repos/" + connectionProperties.username + "/" + REPO_CREATE_NAME, null).getEntity().getContent()))

        then:
        //PackageImpl
        locallyCreated.getType().equals(repo.getType())
        locallyCreated.getName().equals(repo.getName())
        locallyCreated.getIsPrivate().equals(repo.getIsPrivate())
        locallyCreated.getPremium().equals(repo.getPremium())
        locallyCreated.getDesc().equals(repo.getDesc())
        locallyCreated.getLabels().sort().equals(repo.getLabels().sort())

        and:
        //jsons
        REPO_CREATE_NAME.equals(directJson.name)
        repositoryDetails.getDescription().equals(directJson.desc)
        repositoryDetails.getIsPrivate().equals(directJson.private)
        repositoryDetails.getPremium().equals(directJson.premium)
        repositoryDetails.getType().equals(directJson.type)
        for (int i = 0; i < repositoryDetails.getLabels().size(); i++) {
            repositoryDetails.getLabels().sort().get(i).equalsIgnoreCase(directJson.labels.sort()[i])
        }

        cleanup:
        try {
            String cleanPkg = "/repos/" + connectionProperties.username + "/" + REPO_CREATE_NAME
            restClient.delete(cleanPkg, null)
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
    }

    def cleanup() {
        BintraySpecSuite.cleanup()
    }
}