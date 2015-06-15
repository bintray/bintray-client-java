package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.api.model.Subject
import com.jfrog.bintray.client.test.BintraySpecSuite
import com.timgroup.jgravatar.Gravatar
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType
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

    def cleanup() {
        BintraySpecSuite.cleanup()
    }
}