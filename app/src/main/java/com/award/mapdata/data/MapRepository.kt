package com.award.mapdata.data

import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.entity.ViewMapInfo

abstract class MapRepository {
    abstract suspend fun getTopLevelMap(id: String): List<ViewMapInfo>?
}

class EsriMapRepository constructor(
    private val remoteDataSource: MapDataSource<PreplannedMapArea>,
    private val mapDataDomainConverter: MapDataConverter<PreplannedMapArea>): MapRepository() {

    override suspend fun getTopLevelMap(id: String): List<ViewMapInfo>? {
        //TODO support offline first storage here as well
        return remoteDataSource.getMapData(id)?.map {
            mapDataDomainConverter.convertToGenericData(it)
        }
    }
}