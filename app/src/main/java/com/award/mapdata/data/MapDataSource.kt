package com.award.mapdata.data

abstract class MapDataSource<T> {
    abstract suspend fun getMapData(id: String): List<T>?
}
