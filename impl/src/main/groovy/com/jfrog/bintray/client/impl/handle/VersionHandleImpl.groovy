package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.BintrayCallException
import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.PackageHandle
import com.jfrog.bintray.client.api.handle.VersionHandle
import com.jfrog.bintray.client.api.model.Attribute
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
        VersionImpl versionImpl = new VersionImpl(name: data.name, description: data.desc, pkg: data.'package', repository: data.repo, owner: data.owner,
                labels: data.labels, attributeNames: data.attribute_names, ordinal: data.ordinal.toInteger(), vcsTag: data.vcs_tag)
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

    VersionHandle update(VersionDetails versionBuilder) {
        def requestBody = [desc: versionBuilder.description, vcs_tag: versionBuilder.vcsTag]
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

    @Override
    boolean exists() {
        try {
            bintrayHandle.head("packages/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/versions/$name").data
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
    VersionHandle setAttributes(List<Attribute> attributes) {
        bintrayHandle.post("packages/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/versions/$name/attributes", packageHandle.createJsonFromAttributes(attributes))
        this
    }

    VersionHandle upload(Map<String, InputStream> content) {
//        TODO setup asyncClient and enable gpars
//        withPool {
        content.each { path, data ->
            bintrayHandle.putBinary("content/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/$name/$path", data)
//            }
        }
        this
    }

    VersionHandle upload(List<File> content, boolean recursive) {
        throw new UnsupportedOperationException('TODO implement upload of files')
    }

    VersionHandle upload(File directory, boolean recursive) {
        throw new UnsupportedOperationException('TODO implement upload of directories')
    }

    VersionHandle publish(){
        bintrayHandle.post("content/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/$name/publish")
        this
    }

    VersionHandle discard(){
        bintrayHandle.post("content/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/$name/publish", [discard:true])
        this
    }
    VersionHandle sign(String passphrase = ''){
        def path = "gpg/${packageHandle.repository().owner().name()}/${packageHandle.repository().name()}/${packageHandle.name()}/versions/$name"
        passphrase ? bintrayHandle.post(path, [passphrase : passphrase]) : bintrayHandle.post(path)
        this
    }
}
