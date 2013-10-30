package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.api.handle.SubjectHandle
import com.jfrog.bintray.client.api.model.Subject
import com.jfrog.bintray.client.impl.model.SubjectImpl
import org.joda.time.format.ISODateTimeFormat
/**
 * @author Noam Y. Tenne
 */
class SubjectHandleImpl implements SubjectHandle {

    private BintrayImpl bintrayHandle
    private String subject

    SubjectHandleImpl(BintrayImpl bintrayHandle, String subject) {
        this.bintrayHandle = bintrayHandle
        this.subject = subject
    }

    String name() {
        subject
    }

    RepositoryHandle repository(String repoName) {
        new RepositoryHandleImpl(bintrayHandle, this, repoName)
    }

    Subject get() {
        def data = bintrayHandle.get("users/$subject").data
        new SubjectImpl(name: data.name, fullName: data.full_name, gravatarId: data.gravatar_id,
                repositories: data.repos, organizations: data.organizations,
                followersCount: data.followers_count.toInteger(),
                registered: ISODateTimeFormat.dateTime().parseDateTime(data.registered),
                quotaUsedBytes: data.quota_used_bytes.toLong())
    }

}
