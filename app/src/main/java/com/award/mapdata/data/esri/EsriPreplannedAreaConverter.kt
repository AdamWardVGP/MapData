package com.award.mapdata.data.esri

import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapID.*
import com.award.mapdata.data.entity.ViewMapInfo
import javax.inject.Inject

class EsriPreplannedAreaConverter @Inject constructor() : MapDataConverter<PreplannedMapArea>() {
    override fun convertToGenericData(mapData: PreplannedMapArea, downloadState: DownloadState): ViewMapInfo {
        return ViewMapInfo(
            itemId = EsriID(mapData.portalItem.itemId),
            imageUri = mapData.portalItem.thumbnail?.uri,
            title = mapData.portalItem.title,
            description = mapData.portalItem.snippet,
            downloadState = downloadState
        )
    }
}