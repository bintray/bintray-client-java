package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.details.RepositoryDetails
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.api.model.Subject
import com.jfrog.bintray.client.impl.model.RepositoryImpl
import com.timgroup.jgravatar.Gravatar
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType
import org.codehaus.jackson.map.ObjectMapper
import spock.lang.Specification

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PKGS
import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_REPOS
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
        'boxes'   | _
        'registry'| _
        'nuget'   | _
        'opkg'    | _
    }

    def 'search by attributes'() {
        setup:
        def repo = bintray.subject(connectionProperties.username).repository(REPO_NAME)

        repo.createPkg(pkgBuilder)
        String attributesQuery = "[{\"name\": \"" + ATTRIBUTE_NAME + "\", \"values\" : [\"" + ATTRIBUTE_VALUE + "\"], \"type\": \"string\"}]"
        def headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        restClient.post("/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/" + "attributes", headers,
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
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/" + API_REPOS + connectionProperties.username + "/" + REPO_CREATE_NAME, null).getEntity().getContent()))

        then:
        //RepoImpl
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
            String delRepo = "/" + API_REPOS + connectionProperties.username + "/" + REPO_CREATE_NAME
            restClient.delete(delRepo, null)
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
    }

    def 'Repo update using RepositoryDetails'() {
        setup:
        ObjectMapper mapper = new ObjectMapper()
        RepositoryDetails repositoryDetails = mapper.readValue(repoJson, RepositoryDetails.class)
        RepositoryHandle repoHandle = bintray.subject(connectionProperties.username).createRepo(repositoryDetails)

        def newLabels = ['newLabel1', 'newLabel2']
        def newDescription = 'A new description for the updated repo'
        repositoryDetails.setLabels(newLabels)
        repositoryDetails.setDescription(newDescription)
        //We don't expect these to change
        repositoryDetails.setIsPrivate(true)
        repositoryDetails.setPremium(true)
        String updateDetailsJson = RepositoryImpl.getUpdateJson(repositoryDetails)
        RepositoryDetails updateDetails = mapper.readValue(updateDetailsJson, RepositoryDetails.class)

        when:
        repoHandle.update(updateDetails)
        JsonSlurper slurper = new JsonSlurper()
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/" + API_REPOS + connectionProperties.username + "/" + REPO_CREATE_NAME, null).getEntity().getContent()))

        then:
        //PackageImpl
        'maven'.equals(directJson.type)
        updateDetails.getName().equals(directJson.name)
        false.equals(directJson.private)
        false.equals(directJson.premium)
        updateDetails.getDescription().equals(directJson.desc)
        updateDetails.getLabels().sort().equals(directJson.labels.sort())
    }

    def 'Delete repository'(){
        setup:
        ObjectMapper mapper = new ObjectMapper()
        RepositoryDetails repositoryDetails = mapper.readValue(repoJson, RepositoryDetails.class)
        bintray.subject(connectionProperties.username).createRepo(repositoryDetails)

        when:
        bintray.subject(connectionProperties.username).repository(repositoryDetails.name).delete()


        then:
        !bintray.subject(connectionProperties.username).repository(repositoryDetails.name).exists()
    }

    def cleanup() {
        try {
            String repo = "/" + API_REPOS + connectionProperties.username + "/" + REPO_CREATE_NAME
            restClient.delete(repo, null)
        } catch (BintrayCallException bce) {
            if (bce.getStatusCode() != 404) {
                System.err.println("cleanup: " + bce)
            }
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
    }
}