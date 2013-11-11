package com.jfrog.bintray.client.impl

import com.jfrog.bintray.client.api.builder.PackageBuilder
import com.jfrog.bintray.client.api.builder.VersionBuilder
import com.jfrog.bintray.client.api.handle.*
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.api.model.Repository
import com.jfrog.bintray.client.api.model.Subject
import com.jfrog.bintray.client.api.model.Version
import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus
import org.joda.time.DateTime
import spock.lang.Specification
/**
 * @author Noam Y. Tenne
 */
class BintrayClientSpec extends Specification {

    def 'get chain'() {
        setup:
        Bintray create = BintrayClient.create('http://localhost:8080/interaction/api/v1/', 'joebloggs', '7e9de678f3c87f9df037f06d09b157e7a7b1ec6552289d61550b2628a8b9d4bb')

        when:
        SubjectHandle subject = create.currentSubject()
        Subject joe = subject.get()

        then:
        joe.name == 'joebloggs'

        and:
        when:
        RepositoryHandle repository = subject.repository('jimson')
        Repository jimson = repository.get()

        then:
        jimson.name == 'jimson'

        and:
        when:
        PackageHandle packageHandle = repository.pkg('johnsy')
        Pkg johnsy = packageHandle.get()

        then:
        johnsy.name == 'johnsy'

        and:
        when:
        VersionHandle versionHandle = packageHandle.version('555')
        Version version = versionHandle.get()

        then:
        version.name == '555'
    }

    def 'create chain'() {
        setup:
        Bintray create = BintrayClient.create('http://localhost:8080/interaction/api/v1/', 'joebloggs', '7e9de678f3c87f9df037f06d09b157e7a7b1ec6552289d61550b2628a8b9d4bb')
        SubjectHandle subject = create.currentSubject()
        RepositoryHandle centralRepo = subject.repository('jimson')

        PackageHandle newPackage = centralRepo.pkg("pkg${new Random(System.currentTimeMillis()).nextInt()}")
                .create(new PackageBuilder().description('jimson').labels(['label1', 'label2']).licenses(['Apache 2']))

        when:
        Pkg newPackageModel = newPackage.get()

        then:
        newPackageModel.name == newPackage.name
        newPackageModel.owner == 'joebloggs'
        newPackageModel.description == 'jimson'
        newPackageModel.labels == ['label1', 'label2']
        newPackageModel.created
        newPackageModel.updated

        and:
        when:
        DateTime releasedDateTime = new DateTime()
        VersionHandle newVersion = newPackage.version("version${new Random(System.currentTimeMillis()).nextInt()}")
                .create(new VersionBuilder().description('description').released(releasedDateTime))
        Version newVersionModel = newVersion.get()

        then:
        newVersionModel.name == newVersion.name
        newVersionModel.description == 'description'
        newVersionModel.released == releasedDateTime
    }

    def 'delete chain'() {
        setup:
        Bintray create = BintrayClient.create('http://localhost:8080/interaction/api/v1/', 'pigglesmcpiggles', '0b5c74d7040032a5cca4668873a616d7633d1d55d32c32cb7bc81f3c6c8')
        SubjectHandle subject = create.currentSubject()
        RepositoryHandle centralRepo = subject.repository('jimson')
        PackageHandle newPackage = centralRepo.pkg("pkg${new Random(System.currentTimeMillis()).nextInt()}")
                .create(new PackageBuilder().description('jimson').labels(['label1', 'label2']).licenses(['Apache 2']))

        assert newPackage.get()

        DateTime releasedDateTime = new DateTime()
        VersionHandle newVersion = newPackage.version("version${new Random(System.currentTimeMillis()).nextInt()}")
                .create(new VersionBuilder().description('description').released(releasedDateTime))

        assert newVersion.get()

        when:
        newVersion.delete()
        newVersion.get()

        then:
        HttpResponseException ex = thrown(HttpResponseException)
        ex.response.status == HttpStatus.SC_NOT_FOUND

        and:
        when:
        newPackage.delete()
        newPackage.get()

        then:
        ex = thrown(HttpResponseException)
        ex.response.status == HttpStatus.SC_NOT_FOUND
    }
}
