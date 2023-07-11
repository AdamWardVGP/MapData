package com.award.mapdata.data.esri

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.base.MapRepository
import com.award.mapdata.data.entity.MapType
import com.award.mapdata.data.entity.view.RenderableResult
import com.award.mapdata.data.base.DownloadableMapAreaSource
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.base.MapDataSource
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.RepositoryResult
import com.award.mapdata.data.entity.view.ViewMapInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EsriMapRepository @Inject constructor(
    private val remoteMapDataSource: MapDataSource<PortalItem, ArcGISMap>,
    private val mapDataDomainConverter: MapDataConverter<PortalItem>,
    private val remoteDownloadableDataSource: DownloadableMapAreaSource<PreplannedMapArea>
) : MapRepository() {

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

    override fun getMapAreas(id: String): Flow<RepositoryResult<List<ViewMapInfo>>> {
        return remoteDownloadableDataSource.getMapAreas(id).map {
            if(it.itemEntity is RepositoryResult.Failure) {
                RepositoryResult.Failure(it.itemEntity.Exception, it.itemEntity.message)
            } else {
                val convertedItems = it.getItems().map { itemResult ->
                    mapDataDomainConverter.convertToGenericData(
                        MapType.LocalStorage,
                        itemResult.first.portalItem,
                        itemResult.second
                    )
                }
                RepositoryResult.Success(convertedItems)
            }
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
                return remoteDownloadableDataSource.getRenderableMap(id)
            }

            MapType.Unknown -> {
                RepositoryResult.Failure(message = "Invalid Map Type")
            }
        }
    }

    override suspend fun cancelRunningDownloads() {
        remoteDownloadableDataSource.cancelRunningDownloads()
    }

    override suspend fun downloadMapArea(
        parentId: String,
        childId: String
    ): AreaDownloadStatus {
        return remoteDownloadableDataSource.downloadPreplannedArea(parentId, childId)
    }
}