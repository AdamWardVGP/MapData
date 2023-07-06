package com.award.mapdata.data

import com.arcgismaps.LoadStatus
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapID.*
import com.award.mapdata.data.entity.ViewMapInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class EsriMapDataConverter @Inject constructor() : MapDataConverter<PreplannedMapArea>() {
    override fun convertToGenericData(mapData: PreplannedMapArea): ViewMapInfo {
        return ViewMapInfo(
            itemId = EsriID(mapData.portalItem.itemId),
            imageUri = mapData.portalItem.thumbnail?.uri,
            title = mapData.portalItem.title,
            description = mapData.portalItem.description,
            //todo should we just suspend for conversion?
            downloadState = runBlocking {
                when(mapData.loadStatus.first()) {
                    LoadStatus.NotLoaded -> DownloadState.Idle
                    is LoadStatus.FailedToLoad -> DownloadState.Idle
                    is LoadStatus.Loading -> {
                        //TODO does some other API give us progress?
                        DownloadState.Downloading(0.5f)
                    }
                    is LoadStatus.Loaded ->  DownloadState.Downloaded
                }
            }
        )
    }
}