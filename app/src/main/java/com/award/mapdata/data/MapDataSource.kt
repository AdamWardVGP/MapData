package com.award.mapdata.data

abstract class MapDataSource<T> {
    abstract suspend fun getMapData(id: String): T?

    abstract suspend fun getMapAreas(id: String): List<T>?
}
