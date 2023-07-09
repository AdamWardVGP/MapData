package com.award.mapdata.data.base

import com.award.mapdata.data.entity.RepositoryResult

abstract class MapDataSource<T, V> {
    abstract suspend fun getMapData(id: String): RepositoryResult<T>

    abstract fun getRenderableMap(id: String): RepositoryResult<V>

}
