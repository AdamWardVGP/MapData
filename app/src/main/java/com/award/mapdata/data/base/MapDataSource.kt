package com.award.mapdata.data.base

abstract class MapDataSource<T> {
    abstract suspend fun getMapData(id: String): T?

}
