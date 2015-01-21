package com.jfrog.bintray.client.impl

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.details.Attribute
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.Bintray
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.VersionHandle
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.api.model.Subject
import com.jfrog.bintray.client.api.model.Version
import com.jfrog.bintray.client.impl.handle.BintrayImpl
import com.jfrog.bintray.client.impl.model.PackageImpl
import com.jfrog.bintray.client.impl.model.VersionImpl
import com.timgroup.jgravatar.Gravatar
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.apache.http.HttpHeaders
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.entity.ContentType
import org.codehaus.jackson.map.ObjectMapper
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

import java.security.MessageDigest

import static org.apache.http.HttpStatus.SC_NOT_FOUND

/**
 * @author Noam Y. Tenne
 */
class BintrayClientSpec extends Specification {
    private final static String REPO_NAME = 'generic'
    private final static String PKG_NAME = 'bla'
    private final static String VERSION = '1.0'
    public static final String ATTRIBUTE_NAME = 'att1'
    public static final String ATTRIBUTE_VALUE = 'bla'
    @Shared
    private Properties connectionProperties
    @Shared
    private Bintray bintray
    @Shared
    private BintrayImpl restClient  //BintrayImpl can be used as a REST client as well to avoid importing grooyx.net
    @Shared
    private PackageDetails pkgBuilder
    @Shared
    private VersionDetails versionBuilder
    @Shared
    String tempPkgName = "PkgTest"
    @Shared
    private String pkgJson;
    @Shared
    String tempVerName = "3.0"
    @Shared
    private String verJson;

    private ArrayList<Attribute<String>> attributes = [
            new Attribute<String>('a', Attribute.Type.string, "ay1", "ay2"),
            new Attribute<String>('b', 'b', 'e'),
            new Attribute<String>('c', 'cee')]

    private String expectedAttributes = '[[values:[ay1, ay2], name:a, type:string], [values:[cee], name:c, type:string], [values:[e, b], name:b, type:string]]'
    private Map files = ['com/bla/bintray-client-java-api.jar'        : getClass().getResourceAsStream('/bintray-client-java-api.jar'),
                         'org/foo/bar/bintray-client-java-service.jar': getClass().getResourceAsStream('/bintray-client-java-service.jar')]

    private messageDigest = MessageDigest.getInstance("SHA1")
    private String assortedAttributes = "[{\"name\":\"verAttr2\",\"values\":[\"val1\",\"val2\"],\"type\":\"string\"},{\"name\":\"verAttr3\",\"values\":[1,2.2,4],\"type\":\"number\"},{\"name\":\"verAttr2\",\"values\":[\"2011-07-14T19:43:37+0100\"],\"type\":\"date\"}]"

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
        assert this.connectionProperties
        assert this.connectionProperties.username
        assert this.connectionProperties.apiKey
        assert this.connectionProperties.email
        bintray = BintrayClient.create(this.connectionProperties.username as String, this.connectionProperties.apiKey as String, null, null)
        restClient = createClient()
        pkgBuilder = new PackageDetails(PKG_NAME).description('bla-bla').labels(['l1', 'l2']).licenses(['Apache-2.0'])
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
                "    \"released\": \"2015-01-08\",\n" +
                "    \"github_use_tag_release_notes\": false,\n" +
                "    \"vcs_tag\": \"3.8\",\n" +
                "    \"ordinal\": 0,\n" +
                "    \"attributes\": [{\"name\": \"VerAtt1\",\"values\": [\"VerVal1\"],\"type\": \"string\"},\n" +
                "        {\"name\": \"VerAtt2\",\"values\": [1,3.3,5],\"type\": \"number\"},\n" +
                "        {\"name\": \"VerAtt3\",\"values\": [\"2015-01-01T19:43:37+0100\"],\"type\": \"date\"}]\n" +
                "}"

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        //Set level for root logger
        loggerContext.getLogger("ROOT").setLevel(Level.INFO)
        //Disable debug for org.apache.http - you can tweak the level here
        Logger httpLogger = loggerContext.getLogger("org.apache.http");
        httpLogger.setLevel(Level.INFO);
    }

    private BintrayImpl createClient(String url = "https://api.bintray.com") {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(connectionProperties.username as String, connectionProperties.apiKey as String)
        HttpClientConfigurator conf = new HttpClientConfigurator()
        return new BintrayImpl(conf.hostFromUrl(url).noRetry().authentication(creds).getClient(), url)
    }

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

    def 'Package created'() {
        setup:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)

        Map<String, String> headers = new HashMap<>();
        String auth = (connectionProperties.username + ":" + connectionProperties.apiKey)
        headers.put(HttpHeaders.AUTHORIZATION, "Basic " + auth.bytes.encodeBase64())
        String path = "/content/" + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/" + VERSION + "/com/jfrog/bintray/bintray-test/1.0/bintray-test-1.0.pom;publish=1"
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

    def 'attributes set on package'() {
        setup:
        def pkg = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder)

        when:
        pkg.setAttributes(attributes)

        JsonSlurper slurper = new JsonSlurper()
        def actualPackage = slurper.parseText(IOUtils.toString(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME, null).getEntity().getContent()))
        def actualAttributes = slurper.parseText(IOUtils.toString(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME + "/attributes", null).getEntity().getContent()))

        then:
        ['a', 'b', 'c'] == actualPackage.attribute_names.sort()
        and:
        expectedAttributes.equalsIgnoreCase(actualAttributes.sort().toString())
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

    def 'files uploaded and can be accessed by the author'() {
        setup:
        def ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)
        def downloadServerClient = createClient("https://dl.bintray.com")

        when:
        ver.upload(this.files)
        sleep(4000)
        def get1 = downloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(0), null)
        def get2 = downloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(1), null)

        String actual1Sha1 = calculateSha1(get1)
        String actual2Sha1 = calculateSha1(get2)
        then:
        '825e3b98f996498803d8e2da9d834f392fcfc304' == actual1Sha1
        and:
        '5f2a3b521b3ca76f5dac4dd2db123a8a066effe0' == actual2Sha1
    }

    def 'unpublished files can\'t be seen by anonymous'() {
        setup:
        sleep(10000) //wait for previous deletions to propagate
        def ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder)
        HttpClientConfigurator conf = new HttpClientConfigurator();
        def anonymousDownloadServerClient = new BintrayImpl(conf.hostFromUrl("https://dl.bintray.com").noRetry().getClient(), "https://dl.bintray.com")

        when:
        sleep(6000)
        ver.upload(this.files)
        sleep(10000)
        def response = anonymousDownloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(0), null)

        then:
        BintrayCallException bce = thrown()
        bce.getStatusCode().equals(401)
    }

    def 'publish artifacts'() {
        setup:
        VersionHandle ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder).upload(this.files)
        HttpClientConfigurator conf = new HttpClientConfigurator();
        def anonymousDownloadServerClient = new BintrayImpl(conf.hostFromUrl("https://dl.bintray.com").noRetry().getClient(), "https://dl.bintray.com")

        when:
        sleep(2000)
        ver.publish()
        sleep(6000)
        def response = anonymousDownloadServerClient.get("/" + connectionProperties.username + "/" + REPO_NAME + "/" + files.keySet().asList().get(0), null)
        String sha1 = calculateSha1(response)

        then:
        '825e3b98f996498803d8e2da9d834f392fcfc304' == sha1
    }

    def 'discard artifacts'() {
        setup:
        VersionHandle ver = bintray.subject(connectionProperties.username).repository(REPO_NAME).createPkg(pkgBuilder).createVersion(versionBuilder).upload(this.files)
        when:
        ver.discard()
        sleep(4000) //wait for propagation to dl and stuff
        "https://dl.bintray.com/$connectionProperties.username/$REPO_NAME/${files.keySet().asList().get(0)}".toURL().content
        then:
        IOException ioe = thrown()
        ioe.getMessage().contains("401") || ioe instanceof FileNotFoundException
    }


    private String calculateSha1(get) {
        calculateSha1(new ByteArrayInputStream(get.getEntity().getContent().bytes))
    }

    private String calculateSha1(InputStream inputStream) {
        inputStream.eachByte(1024) { byte[] buf, int bytesRead ->
            this.messageDigest.update(buf, 0, bytesRead)
        }
        String actualSha1 = new BigInteger(1, this.messageDigest.digest()).toString(16).padLeft(40, '0')
        actualSha1
    }

    def 'on error response is returned without parsing'() {
        setup:
        Bintray wrongBintray = BintrayClient.create(this.connectionProperties.username as String, this.connectionProperties.apiKey as String, null, null)
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

    def 'wrong repository gives 404'() {
        when:
        bintray.subject(connectionProperties.username).repository('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'wrong package gives 404'() {
        when:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg('bla').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
    }

    def 'wrong version gives 404'() {
        when:
        bintray.subject(connectionProperties.username).repository(REPO_NAME).pkg(PKG_NAME).version('3434').get()
        then:
        BintrayCallException e = thrown()
        e.statusCode == SC_NOT_FOUND
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
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName, null).getEntity().getContent()))
        List<Attribute> attributes = Attribute.getAttributeListFromJson(restClient.get("/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName + "/attributes", null).getEntity().getContent())

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
            String cleanPkg = "/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + tempPkgName
            restClient.delete(cleanPkg, null)
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }


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

        verDetailsFromJson.getReleased().toString().equals(directJson.released)
        verDetailsFromJson.getUseTagReleaseNotes().equals(directJson.github_use_tag_release_notes)
        verDetailsFromJson.getVcsTag().equals(directJson.vcs_tag)
        verDetailsFromJson.getOrdinal().equals(Float.floatToIntBits(directJson.ordinal))

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
        try {
            String pkg = "/packages/" + connectionProperties.username + "/" + REPO_NAME + "/" + PKG_NAME
            restClient.delete(pkg, null)
        } catch (BintrayCallException e) {
            if (e.getStatusCode() != SC_NOT_FOUND) { //don't care
                throw e
            }
        }
    }
}