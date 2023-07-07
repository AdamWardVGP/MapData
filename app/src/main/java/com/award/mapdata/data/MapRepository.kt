package com.award.mapdata.data

import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.base.DownloadableMapAreaSource
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.base.MapDataSource
import com.award.mapdata.data.entity.ViewMapInfo
import javax.inject.Inject

abstract class MapRepository {
    abstract suspend fun getTopLevelMap(id: String): ViewMapInfo?

    abstract suspend fun getMapAreas(id: String): List<ViewMapInfo>?
}

class EsriMapRepository @Inject constructor(
    private val remoteMapDataSource: MapDataSource<PortalItem>,
    private val mapDataDomainConverter: MapDataConverter<PortalItem>,
    private val remoteDownloadableDataSource: DownloadableMapAreaSource<PreplannedMapArea>,
    private val mapAreaDataConverter: MapDataConverter<PreplannedMapArea>
): MapRepository() {

    override suspend fun getTopLevelMap(id: String): ViewMapInfo? {
        //TODO support offline first storage here
        return remoteMapDataSource.getMapData(id)
            ?.let { mapDataDomainConverter.convertToGenericData(it) }
    }

    override suspend fun getMapAreas(id: String): List<ViewMapInfo>? {
        //TODO support offline first storage here as well
        return remoteDownloadableDataSource.getMapAreas(id)?.map {
            mapAreaDataConverter.convertToGenericData(it)
        }
    }
}