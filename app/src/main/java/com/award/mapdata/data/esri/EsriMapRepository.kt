package com.award.mapdata.data.esri

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.base.MapRepository
import com.award.mapdata.data.entity.MapType
import com.award.mapdata.data.entity.RenderableResult
import com.award.mapdata.data.base.DownloadableMapAreaSource
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.base.MapDataSource
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.AreaInfo
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.RepositoryResult
import com.award.mapdata.data.entity.ViewMapInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EsriMapRepository @Inject constructor(
    private val remoteMapDataSource: MapDataSource<PortalItem, ArcGISMap>,
    private val mapDataDomainConverter: MapDataConverter<PortalItem>,
    private val remoteDownloadableDataSource: DownloadableMapAreaSource<PreplannedMapArea, ArcGISMap>
) : MapRepository() {

    //TODO move to offline storage
    //TODO define eviction policy
    private val areaCache = mutableMapOf<String, List<PreplannedMapArea>>()

    override suspend fun getTopLevelMap(id: String): RepositoryResult<ViewMapInfo> {
        //TODO support offline first storage here
        return when (val result = remoteMapDataSource.getMapData(id)) {
            is RepositoryResult.Failure -> {
                RepositoryResult.Failure(result.Exception, result.message)
            }
            is RepositoryResult.Success -> {
                RepositoryResult.Success(
                    mapDataDomainConverter.convertToGenericData(MapType.Remote, result.payload)
                )
            }
        }
    }

    override suspend fun getMapAreas(id: String): RepositoryResult<List<ViewMapInfo>> {
        return when (val result = getMapAreaInternal(id)) {
            is RepositoryResult.Failure -> {
                RepositoryResult.Failure(result.Exception, result.message)
            }
            is RepositoryResult.Success -> {
                val convertedItems = result.payload.map {
                    //TODO: in progress download state isn't supported
                    val downloadState =
                        if (remoteDownloadableDataSource.isAreaDownloaded(it.portalItem.itemId)) {
                            DownloadState.Downloaded
                        } else {
                            DownloadState.Idle
                        }
                    mapDataDomainConverter.convertToGenericData(
                        MapType.LocalStorage,
                        it.portalItem,
                        downloadState
                    )
                }
                RepositoryResult.Success(convertedItems)
            }
        }
    }

    private suspend fun getMapAreaInternal(id: String): RepositoryResult<List<PreplannedMapArea>> {
        //TODO support offline first storage here as well
        val cachedItem = areaCache[id]
        return if (cachedItem != null) {
            RepositoryResult.Success(cachedItem)
        } else {
            remoteDownloadableDataSource.getMapAreas(id)
        }
    }

    override fun deleteDownloadedMapArea(id: String): Boolean {
        return remoteDownloadableDataSource.deletePreplannedArea(id)
    }

    override suspend fun getRenderableMap(
        mapType: MapType,
        id: String
    ): RepositoryResult<RenderableResult> {
        return when (mapType) {
            MapType.Remote -> {
                when (val result = remoteMapDataSource.getRenderableMap(id)) {
                    is RepositoryResult.Failure -> {
                        RepositoryResult.Failure(
                            result.Exception, result.message
                        )
                    }

                    is RepositoryResult.Success -> {
                        RepositoryResult.Success(
                            RenderableResult.ArcGisMap(result.payload)
                        )
                    }
                }
            }

            MapType.LocalStorage -> {
                when (val result =
                    remoteDownloadableDataSource.getRenderableMap(id)
                ) {
                    is RepositoryResult.Failure -> {
                        RepositoryResult.Failure(
                            result.Exception, result.message
                        )
                    }

                    is RepositoryResult.Success -> {
                        RepositoryResult.Success(
                            RenderableResult.ArcGisMap(result.payload)
                        )
                    }
                }
            }

            MapType.Unknown -> {
                RepositoryResult.Failure(message = "Invalid Map Type");
            }
        }
    }

    override suspend fun cancelRunningDownloads() {
        remoteDownloadableDataSource.cancelRunningDownloads()
    }

    override suspend fun downloadMapArea(
        parentId: String,
        childId: String
    ): Flow<AreaDownloadStatus> {

        if (remoteDownloadableDataSource.isAreaDownloaded(childId)) {
            return flowOf(AreaDownloadStatus.Completed)
        }

        when (val result = getMapAreaInternal(parentId)) {
            is RepositoryResult.Failure -> {
                return flowOf(
                    AreaDownloadStatus.Aborted(result.Exception, result.message)
                )
            }

            is RepositoryResult.Success -> {
                //We have to search the map areas belonging to the parent to find a child item.
                //this is since we're using the PreplanedMapArea object in the API to perform the download
                result.payload.firstOrNull { it.portalItem.itemId == childId }?.let { area ->
                    return remoteDownloadableDataSource.downloadPreplannedArea(
                        AreaInfo.EsriMapArea(parentId, area)
                    )
                } ?: return flowOf(
                    AreaDownloadStatus.Aborted(message = "Parent doesn't contain expected child portal ID")
                )
            }
        }
    }
}