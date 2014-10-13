package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.handle.ArtibutesSearchQuery
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.handle.SubjectHandle
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.api.model.Repository
import com.jfrog.bintray.client.impl.model.RepositoryImpl
import groovy.json.JsonBuilder
import org.joda.time.format.ISODateTimeFormat

/**
 * @author Noam Y. Tenne
 */
class RepositoryHandleImpl implements RepositoryHandle {

    private BintrayImpl bintrayHandle
    private SubjectHandleImpl owner
    private String name
    private List<ArtibutesSearchQuery> queries = []

    RepositoryHandleImpl(BintrayImpl bintrayHandle, SubjectHandleImpl owner, String name) {
        this.bintrayHandle = bintrayHandle
        this.owner = owner
        this.name = name
    }

    String name() {
        name
    }

    SubjectHandle owner() {
        owner
    }

    PackageHandle pkg(String packageName) {
        new PackageHandleImpl(bintrayHandle, this, packageName)
    }

    @SuppressWarnings("GroovyAccessibility")
    PackageHandle createPkg(PackageDetails packageDetails) {
        bintrayHandle.post("packages/${this.owner().name()}/${this.name()}", jsonFromPackageDetails(packageDetails))
        new PackageHandleImpl(bintrayHandle, this, packageDetails.name)
    }

    LinkedHashMap<String, Object> jsonFromPackageDetails(PackageDetails packageDetails) {
        [name                   : packageDetails.name,
         desc                   : packageDetails.description,
         labels                 : packageDetails.labels,
         licenses               : packageDetails.licenses,
         vcs_url                : packageDetails.vcsUrl,
         website_url            : packageDetails.websiteUrl,
         issue_tracker_url      : packageDetails.issueTrackerUrl,
         public_download_numbers: packageDetails.publicDownloadNumbers
        ]
    }

    Repository get() {
        def data = bintrayHandle.get("repos/${owner.name()}/$name").data
        new RepositoryImpl(name: data.name, owner: data.owner, desc: data.description, labels: data.labels,
                created: ISODateTimeFormat.dateTime().parseDateTime(data.created),
                packageCount: data.package_count.toInteger())
    }

    ArtibutesSearchQuery searchForPackage() {
        return new ArtibutesSearchQueryImpl<Pkg>(this, bintrayHandle)
    }

    @SuppressWarnings("GroovyAccessibility")
    private List attributeSearch() {
        Map query = queries.collectEntries { ArtibutesSearchQueryImpl query ->
            [query.name, query.queryClauses*.clauseValue]
        }
        bintrayHandle.post("/search/attributes/${owner().name()}/${name()}", new JsonBuilder([query]).toString()).data.collect {
            PackageHandleImpl.createPackageFromJsonMap(it)
        }
    }
}
