package com.award.mapdata.data.entity

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
data class ViewMapInfo(
    val itemId: MapID,
    val imageUri: String?,
    val title: String,
    val description: String,
    val downloadState: DownloadState
)

/**
 * Generalized IDs to ensure strong type matching across calls to data layer
 */
sealed class MapID(val itemKey: String) {
    class EsriID(itemKey: String): MapID(itemKey)
}

sealed class MapItemListElement(val viewType: Int) {
    class Header(val title: String): MapItemListElement(1)
    class MapElement(val mapInfo: ViewMapInfo): MapItemListElement(2)
    object Divider: MapItemListElement(3)
    object Loading: MapItemListElement(4)
}