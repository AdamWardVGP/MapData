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
        val loadValue = mapData.loadStatus.value

        return ViewMapInfo(
            itemId = EsriID(mapData.portalItem.itemId),
            imageUri = mapData.portalItem.thumbnail?.uri,
            title = mapData.portalItem.title,
            description = mapData.portalItem.snippet,
            downloadState = when(loadValue) {
                is LoadStatus.NotLoaded -> DownloadState.Idle
                is LoadStatus.FailedToLoad -> DownloadState.Idle
                is LoadStatus.Loading -> {
                    //TODO PreplannedMapArea doesn't actually know this state.  Download state
                    // comes from OfflineMapTask or, the existence of an entry in our file system.
                    // we need to augment our data by querying for any in-progress jobs if possible,
                    // as well as checking local disk for a cached item.
                    DownloadState.Downloading(0.0f)
                }
                is LoadStatus.Loaded ->  DownloadState.Downloaded
            }
        )
    }
}