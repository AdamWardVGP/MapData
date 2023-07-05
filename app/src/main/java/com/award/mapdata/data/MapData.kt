package com.award.mapdata.data

sealed class DownloadState {

    //Indicates the element cannot support downloading
    object Unavailable: DownloadState()

    // No download has been triggered for this item
    object Idle: DownloadState()

    // Download progress
    // @param progressPercentage [0..1] as a float for 0 -> 100% downloaded
    class Downloading(val progressPercentage: Float): DownloadState()

    object Downloaded: DownloadState()
}

/**
 * Container for view layer rendering of map elements
 */
data class ViewMapInfo(val title: String, val description: String, val downloadState: DownloadState)

sealed class MapItemListElement(val viewType: Int) {
    class Header(val title: String): MapItemListElement(1)
    class MapElement(val mapInfo: ViewMapInfo): MapItemListElement(2)
    object Divider: MapItemListElement(3)
}