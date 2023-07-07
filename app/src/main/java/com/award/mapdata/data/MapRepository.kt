package com.award.mapdata.data

import com.arcgismaps.mapping.PortalItem
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.ViewMapInfo
import javax.inject.Inject

abstract class MapRepository {
    abstract suspend fun getTopLevelMap(id: String): ViewMapInfo?

    abstract suspend fun getMapAreas(id: String): List<ViewMapInfo>?
}

class EsriMapRepository @Inject constructor(
    private val remoteDataSource: MapDataSource<PortalItem>,
    private val mapDataDomainConverter: MapDataConverter<PortalItem>): MapRepository() {

    override suspend fun getTopLevelMap(id: String): ViewMapInfo? {
        //TODO support offline first storage here as well
        return remoteDataSource.getMapData(id)
            ?.let { mapDataDomainConverter.convertToGenericData(it, DownloadState.Unavailable) }
    }

    override suspend fun getMapAreas(id: String): List<ViewMapInfo>? {
        return remoteDataSource.getMapAreas(id)?.map {
            mapDataDomainConverter.convertToGenericData(it)
        }
    }
}