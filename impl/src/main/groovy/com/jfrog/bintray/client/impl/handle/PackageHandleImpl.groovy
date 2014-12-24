package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.BintrayCallException
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.handle.VersionHandle
import com.jfrog.bintray.client.api.model.Attribute
import com.jfrog.bintray.client.api.model.Pkg
import com.jfrog.bintray.client.impl.model.PackageImpl
import org.joda.time.format.ISODateTimeFormat
/**
 * @author Noam Y. Tenne
 */
class PackageHandleImpl implements PackageHandle {

    private BintrayImpl bintrayHandle
    private RepositoryHandleImpl repositoryHandle
    private String name

    PackageHandleImpl(BintrayImpl bintrayHandle, RepositoryHandleImpl repositoryHandle, String name) {
        this.bintrayHandle = bintrayHandle
        this.repositoryHandle = repositoryHandle
        this.name = name
    }

    String name() {
        return name
    }

    RepositoryHandle repository() {
        repositoryHandle
    }

    VersionHandle version(String versionName) {
        new VersionHandleImpl(bintrayHandle, this, versionName)
    }

    Pkg get() {
        def data = bintrayHandle.get("packages/${repositoryHandle.owner().name()}/${repositoryHandle.name()}/$name").data
        createPackageFromJsonMap(data)
    }

    private static PackageImpl createPackageFromJsonMap(data) {
        new PackageImpl(name: data.name, repository: data.repository, owner: data.owner, description: data.desc, labels: data.labels,
                attributeNames: data.attribute_names, rating: data.rating?.toInteger(),
                ratingCount: data.rating_count?.toInteger(), followersCount: data.followers_count?.toInteger(),
                created: ISODateTimeFormat.dateTime().parseDateTime(data.created), versions: data.versions,
                latestVersion: data.latest_version, updated: ISODateTimeFormat.dateTime().parseDateTime(data.updated),
                linkedToRepo: data.linked_to_repo, systemIds: data.system_ids)
    }

    PackageHandle update(PackageDetails packageBuilder) {
        bintrayHandle.patch("packages/${repositoryHandle.owner().name()}/${repositoryHandle.name()}/$name", repositoryHandle.jsonFromPackageDetails(packageBuilder))
        this
    }

    PackageHandle delete() {
        bintrayHandle.delete("packages/${repositoryHandle.owner().name()}/${repositoryHandle.name()}/$name")
        this
    }

    @Override
    boolean exists() {
        try {
            bintrayHandle.head("packages/${repositoryHandle.owner().name()}/${repositoryHandle.name()}/$name").data
        } catch (BintrayCallException e) {
            if (e.getStatusCode() == 404) {
                return false
            } else {
                throw e
            }
        }
        return true
    }

    @Override
    VersionHandle createVersion(VersionDetails versionDetails) {
        def requestBody = [name: versionDetails.name, desc: versionDetails.description, vcs_tag: versionDetails.vcsTag]
        if (versionDetails.released) {
            requestBody.released = ISODateTimeFormat.dateTime().print(versionDetails.released)
        }
        bintrayHandle.post("packages/${this.repository().owner().name()}/${this.repository().name()}/${this.name()}/versions", requestBody)
        new VersionHandleImpl(bintrayHandle, this, versionDetails.name)
    }

    @Override
    PackageHandle setAttributes(List<Attribute> attributes) {
        bintrayHandle.post("packages/${repositoryHandle.owner().name()}/${repositoryHandle.name()}/$name/attributes", createJsonFromAttributes(attributes))
        this
    }

    public List<Map<String, Object>> createJsonFromAttributes(List<Attribute> attributes) {
        attributes.collect { Attribute attribute ->
            def attr = [name: attribute.name(), values: attribute.values()]
            if (attribute.type()) {
                attr.type = attribute.type().name().toLowerCase()
            }
            attr
        }
    }
}
