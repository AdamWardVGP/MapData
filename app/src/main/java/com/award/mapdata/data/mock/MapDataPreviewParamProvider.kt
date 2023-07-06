package com.award.mapdata.data.mock

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapID
import com.award.mapdata.data.entity.MapItemListElement
import com.award.mapdata.data.entity.MapItemListElement.*
import com.award.mapdata.data.entity.ViewMapInfo
import com.award.mapdata.data.mock.MapPreviewData.dataElements

class MapDataPreviewParamProvider
    : PreviewParameterProvider<List<MapItemListElement>> {
    override val values: Sequence<List<MapItemListElement>> = sequenceOf(dataElements)
}

object MapPreviewData {

    val unavailableDownloadMapInfoSample = ViewMapInfo(
        itemId = MapID.EsriID(""),
        title = "Maine",
        imageUri = "",
        description = "Maine is a state in the new england region of the northeastern United States. Main is the 12th smallest by area, the 9th least",
        downloadState = DownloadState.Unavailable
    )

    val idleDownloadMapInfoSample = ViewMapInfo(
        itemId = MapID.EsriID(""),
        title = "Acadia",
        imageUri = "",
        description = "Acadia National Park is an American national park located int he state of Maine, southwest of Bar Harbor.",
        downloadState = DownloadState.Idle
    )

    val downloadingMapInfoSample = ViewMapInfo(
        itemId = MapID.EsriID(""),
        title = "Boston",
        imageUri = "",
        description = "Maine is a state int he New England region of the northeastern United States. Maine is the 12th smallest",
        downloadState = DownloadState.Downloading(0.57f)
    )

    val downloadedMapInfoSample = ViewMapInfo(
        itemId = MapID.EsriID(""),
        title = "Baxter State Park",
        imageUri = "",
        description = "Baxter State Park is a large wilderness area permanently preserved as a state park, located in",
        downloadState = DownloadState.Downloaded
    )

    val dataElements = listOf(
        Header("Web Maps"),
        MapElement(mapInfo = unavailableDownloadMapInfoSample),
        Divider,
        Header("Map areas"),
        MapElement(mapInfo = idleDownloadMapInfoSample),
        MapElement(mapInfo = downloadingMapInfoSample),
        MapElement(mapInfo = downloadedMapInfoSample),
        MapElement(
            mapInfo = ViewMapInfo(
                itemId = MapID.EsriID(""),
                title = "Greater Portland",
                imageUri = "",
                description = "The Greater Portland metropolitan area is home to over half a million people, more than one-third of",
                downloadState = DownloadState.Idle
            )
        ),
        MapElement(
            mapInfo = ViewMapInfo(
                itemId = MapID.EsriID(""),
                title = "Caribou",
                imageUri = "",
                description = "Caribou is the second largest city in Aroostook County, Maine, United States. Its population was 8189 at",
                downloadState = DownloadState.Downloading(0.22f)
            )
        ),
        MapElement(
            mapInfo = ViewMapInfo(
                itemId = MapID.EsriID(""),
                title = "Bangor",
                imageUri = "",
                description = "Bangor is a city in the U.S. state of Maine, and the county seat of Penobscot County. The city proper",
                downloadState = DownloadState.Downloaded
            )
        ),
    )
}