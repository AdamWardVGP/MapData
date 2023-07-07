package com.award.mapdata.data.base

abstract class DownloadableMapAreaSource<T> {
    abstract suspend fun getMapAreas(id: String): List<T>?
}
