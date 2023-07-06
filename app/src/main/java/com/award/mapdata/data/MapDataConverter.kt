package com.award.mapdata.data

import com.award.mapdata.data.entity.ViewMapInfo

abstract class MapDataConverter<T> {
    abstract fun convertToGenericData(mapData: T) : ViewMapInfo
}
