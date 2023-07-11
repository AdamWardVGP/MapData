package com.award.mapdata.data.esri

import com.arcgismaps.mapping.PortalItem
import com.award.mapdata.data.entity.MapType
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.view.DownloadViewState
import com.award.mapdata.data.entity.MapID.*
import com.award.mapdata.data.entity.view.ViewMapInfo
import javax.inject.Inject

class EsriPortalItemConverter @Inject constructor() : MapDataConverter<PortalItem>() {
    override fun convertToGenericData(
        mapType: MapType,
        mapData: PortalItem,
        downloadState: AreaDownloadStatus?
    ): ViewMapInfo {
        return ViewMapInfo(
            itemId = EsriID(mapType, mapData.itemId),
            imageUri = mapData.thumbnail?.uri,
            title = mapData.title,
            description = mapData.snippet,
            downloadViewState = when(downloadState) {
                AreaDownloadStatus.Completed -> DownloadViewState.Downloaded
                is AreaDownloadStatus.Aborted -> DownloadViewState.Idle
                AreaDownloadStatus.Idle -> DownloadViewState.Idle
                is AreaDownloadStatus.InProgress -> DownloadViewState.Downloading(downloadState.progress.toFloat() / 100)
                AreaDownloadStatus.Starting -> DownloadViewState.Downloading(0f)
                null -> DownloadViewState.Unavailable
            }
        )
    }
}