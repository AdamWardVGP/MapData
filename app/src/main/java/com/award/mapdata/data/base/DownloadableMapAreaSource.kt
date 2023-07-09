package com.award.mapdata.data.base

import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.AreaInfo
import com.award.mapdata.data.entity.RepositoryResult
import kotlinx.coroutines.flow.Flow

abstract class DownloadableMapAreaSource<T, V> {
    abstract suspend fun getMapAreas(id: String): RepositoryResult<List<T>>

    abstract fun isAreaDownloaded(areaId: String): Boolean

    abstract suspend fun downloadPreplannedArea(area: AreaInfo): Flow<AreaDownloadStatus>

    abstract fun deletePreplannedArea(id: String): Boolean

    abstract suspend fun getRenderableMap(id: String): RepositoryResult<V>

    abstract suspend fun cancelRunningDownloads()
}
