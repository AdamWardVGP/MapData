package com.award.mapdata.data.base

import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.MapType
import com.award.mapdata.data.entity.view.ViewMapInfo

abstract class MapDataConverter<T> {
    abstract fun convertToGenericData(
        mapType: MapType,
        mapData: T,
        downloadState: AreaDownloadStatus? = null
    ): ViewMapInfo
}
