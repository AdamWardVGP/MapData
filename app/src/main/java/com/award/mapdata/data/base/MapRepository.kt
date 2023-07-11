package com.award.mapdata.data.base

import com.award.mapdata.data.entity.MapType
import com.award.mapdata.data.entity.view.RenderableResult
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.RepositoryResult
import com.award.mapdata.data.entity.view.ViewMapInfo
import kotlinx.coroutines.flow.Flow

abstract class MapRepository {
    abstract suspend fun getTopLevelMap(id: String): RepositoryResult<ViewMapInfo>

    abstract fun getMapAreas(id: String): Flow<RepositoryResult<List<ViewMapInfo>>>

    abstract suspend fun downloadMapArea(
        parentId: String,
        childId: String
    ): AreaDownloadStatus

    abstract fun deleteDownloadedMapArea(id: String): Boolean

    abstract suspend fun getRenderableMap(
        mapType: MapType,
        id: String
    ): RepositoryResult<RenderableResult>

    abstract suspend fun cancelRunningDownloads()
}

