package com.award.mapdata.data.base

import com.arcgismaps.mapping.ArcGISMap
import com.award.mapdata.data.MapType
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.AreaInfo
import kotlinx.coroutines.flow.Flow

abstract class DownloadableMapAreaSource<T> {
    abstract suspend fun getMapAreas(id: String): List<T>?

    abstract fun isAreaDownloaded(areaId: String): Boolean

    abstract suspend fun downloadPreplannedArea(area: AreaInfo): Flow<AreaDownloadStatus>

    abstract fun deletePreplannedArea(id: String): Boolean

    abstract suspend fun getRenderableMap(mapType: MapType, id: String): ArcGISMap?
}
