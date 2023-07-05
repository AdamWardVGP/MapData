package com.award.mapdata.data

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.award.mapdata.data.MapItemListElement.*
import com.award.mapdata.data.MapPreviewData.dataElements

class MapDataPreviewParamProvider
    : PreviewParameterProvider<List<MapItemListElement>> {
    override val values: Sequence<List<MapItemListElement>> = sequenceOf(dataElements)
}

object MapPreviewData {

    val unavailableDownloadMapInfoSample = ViewMapInfo(
        title = "Maine",
        description = "Maine is a state in the new england region of the northeastern United States. Main is the 12th smallest by area, the 9th least",
        downloadState = DownloadState.Unavailable
    )

    val idleDownloadMapInfoSample = ViewMapInfo(
        title = "Acadia",
        description = "Acadia National Park is an American national park located int he state of Maine, southwest of Bar Harbor.",
        downloadState = DownloadState.Idle
    )

    val downloadingMapInfoSample = ViewMapInfo(
        title = "Boston",
        description = "Maine is a state int he New England region of the northeastern United States. Maine is the 12th smallest",
        downloadState = DownloadState.Downloading(0.57f)
    )

    val downloadedMapInfoSample = ViewMapInfo(
        title = "Baxter State Park",
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
                title = "Greater Portland",
                description = "The Greater Portland metropolitan area is home to over half a million people, more than one-third of",
                downloadState = DownloadState.Idle
            )
        ),
        MapElement(
            mapInfo = ViewMapInfo(
                title = "Caribou",
                description = "Caribou is the second largest city in Aroostook County, Maine, United States. Its population was 8189 at",
                downloadState = DownloadState.Downloading(0.22f)
            )
        ),
        MapElement(
            mapInfo = ViewMapInfo(
                title = "Bangor",
                description = "Bangor is a city in the U.S. state of Maine, and the county seat of Penobscot County. The city proper",
                downloadState = DownloadState.Downloaded
            )
        ),
    )
}