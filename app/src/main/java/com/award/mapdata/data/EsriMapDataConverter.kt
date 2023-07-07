package com.award.mapdata.data

import com.arcgismaps.LoadStatus
import com.arcgismaps.mapping.PortalItem
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapID.*
import com.award.mapdata.data.entity.ViewMapInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class EsriMapDataConverter @Inject constructor() : MapDataConverter<PortalItem>() {
    override fun convertToGenericData(mapData: PortalItem, overrideDownloadState: DownloadState?): ViewMapInfo {
        return ViewMapInfo(
            itemId = EsriID(mapData.itemId),
            imageUri = mapData.thumbnail?.uri,
            title = mapData.title,
            description = mapData.snippet,
            downloadState = overrideDownloadState ?: when(mapData.loadStatus.value) {
                is LoadStatus.NotLoaded -> DownloadState.Idle
                is LoadStatus.FailedToLoad -> DownloadState.Idle
                is LoadStatus.Loading -> {
                    //TODO does some other API give us progress?
                    DownloadState.Downloading(0.5f)
                }
                is LoadStatus.Loaded ->  DownloadState.Downloaded
            }
        )
    }
}