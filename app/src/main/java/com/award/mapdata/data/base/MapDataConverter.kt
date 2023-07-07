package com.award.mapdata.data.base

import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.ViewMapInfo

abstract class MapDataConverter<T> {
    abstract fun convertToGenericData(
        mapData: T,
        downloadState: DownloadState = DownloadState.Unavailable
    ): ViewMapInfo
}
