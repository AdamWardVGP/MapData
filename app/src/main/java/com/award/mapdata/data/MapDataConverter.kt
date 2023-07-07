package com.award.mapdata.data

import com.arcgismaps.mapping.PortalItem
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.ViewMapInfo

abstract class MapDataConverter<T> {
    abstract fun convertToGenericData(
        mapData: PortalItem,
        overrideDownloadState: DownloadState? = null
    ): ViewMapInfo
}
