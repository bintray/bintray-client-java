package com.jfrog.bintray.client.impl.handle

import com.jfrog.bintray.client.api.handle.ArtibutesSearchQuery
import com.jfrog.bintray.client.api.handle.AttributesSearchQueryClause
/**
 *
 * @author jbaruch
 * @since 13/11/13
 */
class ArtibutesSearchQueryImpl<T> implements ArtibutesSearchQuery {

    private BintrayImpl bintrayHandle
    private final RepositoryHandleImpl repositoryHandle
    private String name

    ArtibutesSearchQueryImpl(RepositoryHandleImpl repositoryHandle, BintrayImpl bintrayHandle) {
        this.repositoryHandle = repositoryHandle
        this.bintrayHandle = bintrayHandle
    }
    private List<AttributesSearchQueryClauseImpl> queryClauses = []

    AttributesSearchQueryClause byAttributeName(String name){
        repositoryHandle.queries << this
        this.name = name
        new AttributesSearchQueryClauseImpl<T>(this)
    }

    List<T> search(){
        repositoryHandle.queries << this
        repositoryHandle.attributeSearch()
    }

}
