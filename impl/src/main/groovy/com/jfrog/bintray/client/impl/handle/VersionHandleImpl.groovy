package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.builder.VersionBuilder
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.VersionHandle
import com.jfrog.bintray.client.api.model.Version
import com.jfrog.bintray.client.impl.model.VersionImpl
import org.joda.time.format.ISODateTimeFormat

/**
 * @author Noam Y. Tenne
 */
class VersionHandleImpl implements VersionHandle {

    private BintrayImpl bintrayHandle
    private String name
    private PackageHandleImpl packageHandle

    VersionHandleImpl(BintrayImpl bintrayHandle, PackageHandleImpl packageHandle, String name) {
        this.bintrayHandle = bintrayHandle
        this.packageHandle = packageHandle
        this.name = name
    }

    String name() {
        return name
    }

    PackageHandle pkg() {
        packageHandle
    }

    Version get() {
        def data = bintrayHandle.get("packages/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/versions/$name").data
        VersionImpl versionImpl = new VersionImpl(name: data.name, description: data.desc, pkg: data.pkg, repository: data.repository, owner: data.owner,
                labels: data.labels, attributeNames: data.attribute_names, ordinal: data.ordinal.toInteger())
        if (data.created) {
            versionImpl.created = ISODateTimeFormat.dateTime().parseDateTime(data.created)
        }
        if (data.updated) {
            versionImpl.updated = ISODateTimeFormat.dateTime().parseDateTime(data.updated)
        }
        if (data.released) {
            versionImpl.released = ISODateTimeFormat.dateTime().parseDateTime(data.released)
        }
        versionImpl
    }

    VersionHandle create(VersionBuilder versionBuilder) {
        def requestBody = [name: name, desc: versionBuilder.description]
        if (versionBuilder.released) {
            requestBody.released = ISODateTimeFormat.dateTime().print(versionBuilder.released)
        }
        bintrayHandle.post("packages/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/versions", requestBody)
        this
    }

    VersionHandle update(VersionBuilder versionBuilder) {
        def requestBody = [desc: versionBuilder.description]
        if (versionBuilder.released) {
            requestBody.released = ISODateTimeFormat.dateTime().print(versionBuilder.released)
        }
        bintrayHandle.patch("packages/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/versions/$name", requestBody)
        this
    }

    VersionHandle delete() {
        bintrayHandle.delete("packages/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/versions/$name")
        this
    }
}
