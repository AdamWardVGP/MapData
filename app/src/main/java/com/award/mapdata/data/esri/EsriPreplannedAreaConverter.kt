package com.award.mapdata.data.esri

import com.arcgismaps.LoadStatus
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapID.*
import com.award.mapdata.data.entity.ViewMapInfo
import javax.inject.Inject

class EsriPreplannedAreaConverter @Inject constructor() : MapDataConverter<PreplannedMapArea>() {
    override fun convertToGenericData(mapData: PreplannedMapArea): ViewMapInfo {
        return ViewMapInfo(
            itemId = EsriID(mapData.portalItem.itemId),
            imageUri = mapData.portalItem.thumbnail?.uri,
            title = mapData.portalItem.title,
            description = mapData.portalItem.snippet,
            downloadState = when(mapData.loadStatus.value) {
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