package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.handle.SubjectHandle
import com.jfrog.bintray.client.api.model.Repository
import com.jfrog.bintray.client.impl.model.RepositoryImpl
import org.joda.time.format.ISODateTimeFormat
/**
 * @author Noam Y. Tenne
 */
class RepositoryHandleImpl implements RepositoryHandle {

    private BintrayImpl bintrayHandle
    private SubjectHandleImpl owner
    private String name

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

    Repository get() {
        def data = bintrayHandle.get("repos/${owner.name()}/$name").data
        new RepositoryImpl(name: data.name, owner: data.owner, desc: data.description, labels: data.labels,
                created: ISODateTimeFormat.dateTime().parseDateTime(data.created),
                packageCount: data.package_count.toInteger())
    }
}
