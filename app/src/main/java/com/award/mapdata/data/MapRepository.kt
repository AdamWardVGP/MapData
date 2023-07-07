package com.award.mapdata.data

import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.base.DownloadableMapAreaSource
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.base.MapDataSource
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.AreaInfo
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.ViewMapInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

abstract class MapRepository {
    abstract suspend fun getTopLevelMap(id: String): ViewMapInfo?

    abstract suspend fun getMapAreas(id: String): List<ViewMapInfo>?

    abstract suspend fun downloadMapArea(parentId: String, childId: String): Flow<AreaDownloadStatus>?

    abstract fun deleteDownloadedMapArea(id: String): Boolean
}

class EsriMapRepository @Inject constructor(
    private val remoteMapDataSource: MapDataSource<PortalItem>,
    private val mapDataDomainConverter: MapDataConverter<PortalItem>,
    private val remoteDownloadableDataSource: DownloadableMapAreaSource<PreplannedMapArea>,
    private val mapAreaDataConverter: MapDataConverter<PreplannedMapArea>
) : MapRepository() {

    //TODO move to offline storage
    //TODO define eviction policy
    private val areaCache = mutableMapOf<String, List<PreplannedMapArea>>()

    override suspend fun getTopLevelMap(id: String): ViewMapInfo? {
        //TODO support offline first storage here
        return remoteMapDataSource.getMapData(id)
            ?.let { mapDataDomainConverter.convertToGenericData(it) }
    }

    override suspend fun getMapAreas(id: String): List<ViewMapInfo>? {
        return getMapAreaInternal(id)?.map {
            val downloadState = if(remoteDownloadableDataSource.isAreaDownloaded(it.portalItem.itemId)) {
                DownloadState.Downloaded
            } else {
                DownloadState.Idle
            }
            mapAreaDataConverter.convertToGenericData(it, downloadState)
        }
    }

    private suspend fun getMapAreaInternal(id: String): List<PreplannedMapArea>? {
        //TODO support offline first storage here as well
        return if (areaCache.containsKey(id)) {
            areaCache[id]
        } else {
            remoteDownloadableDataSource.getMapAreas(id)
        }
    }

    override fun deleteDownloadedMapArea(id: String): Boolean {
        return remoteDownloadableDataSource.deletePreplannedArea(id)
    }

    override suspend fun downloadMapArea(parentId: String, childId: String): Flow<AreaDownloadStatus>? {

        if(remoteDownloadableDataSource.isAreaDownloaded(childId)) {
            return flowOf(AreaDownloadStatus.Completed)
        }

        //TODO this is actually a parent id containing multiple preplanned areas.
        //we need to reference both the parent, and the child, or have a way to access the children
        getMapAreaInternal(parentId)?.let { parentMap ->
            parentMap.firstOrNull() { it.portalItem.itemId == childId }?.let { area ->
                return remoteDownloadableDataSource.downloadPreplannedArea(
                    AreaInfo.EsriMapArea(parentId, area)
                )
            } ?: return flowOf(AreaDownloadStatus.Aborted)
        } ?: return flowOf(AreaDownloadStatus.Aborted)
    }
}