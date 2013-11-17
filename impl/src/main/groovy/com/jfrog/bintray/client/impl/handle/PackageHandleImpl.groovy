package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.handle.VersionHandle
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
        def requestBody = [desc: packageBuilder.description, labels: packageBuilder.labels,
                licenses: packageBuilder.licenses]
        bintrayHandle.patch("packages/${repositoryHandle.owner().name()}/${repositoryHandle.name()}/$name", requestBody)
        this
    }

    PackageHandle delete() {
        bintrayHandle.delete("packages/${repositoryHandle.owner().name()}/${repositoryHandle.name()}/$name")
        this
    }

    @Override
    VersionHandle createVersion(VersionDetails versionDetails) {
        def requestBody = [name: versionDetails.name, desc: versionDetails.description]
        if (versionDetails.released) {
            requestBody.released = ISODateTimeFormat.dateTime().print(versionDetails.released)
        }
        bintrayHandle.post("packages/${this.repository().owner().name()}/${this.repository().name()}/${this.name()}/versions", requestBody)
        new VersionHandleImpl(bintrayHandle, this, versionDetails.name)
    }
}
