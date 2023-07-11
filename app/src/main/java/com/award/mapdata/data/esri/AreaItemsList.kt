package com.award.mapdata.data.esri

import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.RepositoryResult

/**
 * Wrapper around item results to provide a more readable interface around data access
 */
data class AreaItemsList<T>(val itemEntity: RepositoryResult<List<Pair<T, AreaDownloadStatus>>>?) {

    val isSuccess
        get() = itemEntity is RepositoryResult.Success

    /**
     * Returns item elements of a successful result, or empty list if it's a failed result
     */
    fun getItems(): List<Pair<T, AreaDownloadStatus>> {
        return if(itemEntity is RepositoryResult.Success) {
            itemEntity.payload
        } else {
            listOf()
        }
    }

}