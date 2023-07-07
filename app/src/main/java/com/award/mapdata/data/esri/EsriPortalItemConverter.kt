package com.award.mapdata.data.esri

import com.arcgismaps.mapping.PortalItem
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapID.*
import com.award.mapdata.data.entity.ViewMapInfo
import javax.inject.Inject

class EsriPortalItemConverter @Inject constructor() : MapDataConverter<PortalItem>() {
    override fun convertToGenericData(mapData: PortalItem): ViewMapInfo {
        return ViewMapInfo(
            itemId = EsriID(mapData.itemId),
            imageUri = mapData.thumbnail?.uri,
            title = mapData.title,
            description = mapData.snippet,
            downloadState = DownloadState.Unavailable
        )
    }
}