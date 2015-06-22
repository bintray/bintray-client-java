package com.jfrog.bintray.client.test

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.details.Attribute
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.Bintray
import com.jfrog.bintray.client.impl.BintrayClient
import com.jfrog.bintray.client.impl.HttpClientConfigurator
import com.jfrog.bintray.client.impl.handle.BintrayImpl
import com.jfrog.bintray.client.test.spec.BintrayClientSpec
import com.jfrog.bintray.client.test.spec.PackageSpec
import com.jfrog.bintray.client.test.spec.RepoSpec
import com.jfrog.bintray.client.test.spec.VersionSpec
import org.apache.http.auth.UsernamePasswordCredentials
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.slf4j.LoggerFactory
import spock.lang.Shared

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PKGS
import static org.apache.http.HttpStatus.SC_NOT_FOUND

/**
 * @author Dan Feldman
 */
@RunWith(Suite)
@Suite.SuiteClasses([RepoSpec.class, PackageSpec.class, VersionSpec.class, BintrayClientSpec.class])
class BintraySpecSuite {

    public static final String REPO_CREATE_NAME = 'repoTest'
    public static final String REPO_NAME = 'generic'
    public static final String PKG_NAME = 'bla'
    public static final String VERSION = '1.0'
    public static final String ATTRIBUTE_NAME = 'att1'
    public static final String ATTRIBUTE_VALUE = 'bla'
    @Shared
    public static Properties connectionProperties
    @Shared
    public static Bintray bintray
    @Shared
    public static BintrayImpl restClient
    @Shared
    public static PackageDetails pkgBuilder
    @Shared
    public static VersionDetails versionBuilder
    @Shared
    public static String tempPkgName = "PkgTest"
    @Shared
    public static String tempVerName = "3.0"
    @Shared
    public static String pkgJson
    @Shared
    public static String verJson
    @Shared
    public static String repoJson

    public static ArrayList<Attribute<String>> attributes = [
            new Attribute<String>('a', Attribute.Type.string, "ay1", "ay2"),
            new Attribute<String>('b', 'b', 'e'),
            new Attribute<String>('c', 'cee')]


    public static String expectedPkgAttributes =
            '[[values:[ay1, ay2], name:a, type:string], [values:[cee], name:c, type:string], [values:[e, b], name:b, type:string]]'
    public static String expectedVerAttributes =
            '[[values:[ay1, ay2], name:a, type:string], [values:[cee], name:c, type:string], [values:[e, b], name:b, type:string]]'

    public
    static String assortedAttributes = "[{\"name\":\"verAttr2\",\"values\":[\"val1\",\"val2\"],\"type\":\"string\"}," +
            "{\"name\":\"verAttr3\",\"values\":[1,2.2,4],\"type\":\"number\"},{\"name\":\"verAttr2\"," +
            "\"values\":[\"2011-07-14T19:43:37+0100\"],\"type\":\"date\"}]"

    @BeforeClass
    def static void setup() {
        connectionProperties = new Properties()
        def streamFromProperties = this.class.getResourceAsStream('/bintray-client.properties')
        if (streamFromProperties) {
            streamFromProperties.withStream {
                connectionProperties.load(it)
            }
        }
        def usernameFromEnv = System.getenv('BINTRAY_USERNAME')
        if (usernameFromEnv) {
            connectionProperties.username = usernameFromEnv
        }
        def apiKeyFromEnv = System.getenv('BINTRAY_API_KEY')
        if (apiKeyFromEnv) {
            connectionProperties.apiKey = apiKeyFromEnv
        }
        def emailFromEnv = System.getenv('BINTRAY_EMAIL')
        if (emailFromEnv) {
            connectionProperties.email = emailFromEnv
        }
        assert connectionProperties
        assert connectionProperties.username
        assert connectionProperties.apiKey
        assert connectionProperties.email

        bintray = BintrayClient.create(connectionProperties.username as String, connectionProperties.apiKey as String)

        restClient = createClient()

        pkgBuilder = new PackageDetails(PKG_NAME).description('bla-bla').labels(['l1', 'l2'])
                .licenses(['Apache-2.0']).vcsUrl("https://github.com/bintray/bintray-client-java.git")

        versionBuilder = new VersionDetails(VERSION).description('versionDesc')

        pkgJson = "{\n" +
                "\t\t\"name\": \"" + tempPkgName + "\",\n" +
                "\t\t\"repo\": \"generic\",\n" +
                "\t\t\"owner\": \"" + connectionProperties.username + "\",\n" +
                "\t\t\"desc\": \"Bintray Client Java\",\n" +
                "\t\t\"website_url\": \"http://www.jfrog.com\",\n" +
                "\t\t\"issue_tracker_url\": \"https://github.com/bintray/bintray-client-java/issues\",\n" +
                "\t\t\"vcs_url\": \"https://github.com/bintray/bintray-client-java.git\",\n" +
                "\t\t\"licenses\": [\"MIT\"],\n" +
                "\t\t\"labels\": [\"cool\", \"awesome\", \"gorilla\"],\n" +
                "\t\t\"public_download_numbers\": false,\n" +
                "\t\t\"attributes\": [{\"name\": \"att1\", \"values\" : [\"val1\"], \"type\": \"string\"},\n" +
                "\t\t\t\t\t   {\"name\": \"att3\", \"values\" : [1, 3.3, 5], \"type\": \"number\"},\n" +
                "\t\t\t\t\t   {\"name\": \"att5\", \"values\" : [\"2011-07-14T19:43:37+0100\"], \"type\": \"date\"}]\n" +
                "}"

        verJson = "{\n" +
                "    \"name\": \"" + tempVerName + "\",\n" +
                "    \"desc\": \"Version Test\",\n" +
                "    \"package\": \"" + tempPkgName + "\",\n" +
                "    \"repo\": \"generic\",\n" +
                "    \"owner\": \"" + connectionProperties.username + "\",\n" +
                "    \"labels\": [\"cool\",\"awesome\",\"gorilla\"],\n" +
                "    \"attribute_names\": [\"verAtt1\",\"verAtt2\",\"verAtt3\"],\n" +
                "    \"released\": \" 2015-01-07T18:00:00.000-06:00\",\n" +
                "    \"github_use_tag_release_notes\": false,\n" +
                "    \"vcs_tag\": \"3.8\",\n" +
                "    \"ordinal\": 0,\n" +
                "    \"attributes\": [{\"name\": \"VerAtt1\",\"values\": [\"VerVal1\"],\"type\": \"string\"},\n" +
                "        {\"name\": \"VerAtt2\",\"values\": [1,3.3,5],\"type\": \"number\"},\n" +
                "        {\"name\": \"VerAtt3\",\"values\": [\"2015-01-01T19:43:37+0100\"],\"type\": \"date\"}]\n" +
                "}"

        repoJson = "{\n" +
                "  \"name\": \"" + REPO_CREATE_NAME + "\",\n" +
                "  \"type\": \"maven\",\n" +
                "  \"private\": false,\n" +
                "  \"premium\": false,\n" +
                "  \"desc\": \"Test Repo\",\n" +
                "  \"labels\":[\"lable1\", \"label2\"]\n" +
                "}"

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        //Set level for root logger
        loggerContext.getLogger("ROOT").setLevel(Level.INFO)
        //Disable debug for org.apache.http - you can tweak the level here
        Logger httpLogger = loggerContext.getLogger("org.apache.http");
        httpLogger.setLevel(Level.INFO);
    }

    public static BintrayImpl createClient(String url = "https://api.bintray.com") {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(connectionProperties.username as String,
                connectionProperties.apiKey as String)

        HttpClientConfigurator conf = new HttpClientConfigurator()
        return new BintrayImpl(conf.hostFromUrl(url).noRetry().authentication(creds).getClient(), url, 5, 90000)
    }

    @AfterClass
    public static void cleanup() {
        try {
            String pkg = "/" + API_PKGS + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME
            restClient.delete(pkg, null)
        } catch (BintrayCallException e) {
            if (e.getStatusCode() != SC_NOT_FOUND) { //don't care
                throw e
            }
        }
    }
}
