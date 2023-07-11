package com.award.mapdata.data.base

import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.RepositoryResult
import com.award.mapdata.data.entity.view.RenderableResult
import com.award.mapdata.data.esri.AreaItemsList
import kotlinx.coroutines.flow.StateFlow

abstract class DownloadableMapAreaSource<T> {
    abstract fun getMapAreas(id: String): StateFlow<AreaItemsList<T>>

    abstract suspend fun downloadPreplannedArea(
        parentId: String,
        childId: String
    ): AreaDownloadStatus

    abstract suspend fun getRenderableMap(id: String): RepositoryResult<RenderableResult>

    abstract fun deletePreplannedArea(id: String): Boolean

    abstract suspend fun cancelRunningDownloads()
}
