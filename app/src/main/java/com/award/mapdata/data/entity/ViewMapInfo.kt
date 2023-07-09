package com.award.mapdata.data.entity

import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea

sealed class DownloadState {

    //Indicates the element cannot support downloading
    object Unavailable : DownloadState()

    // No download has been triggered for this item
    object Idle : DownloadState()

    // Download progress
    // @param progressPercentage [0..1] as a float for 0 -> 100% downloaded
    class Downloading(val progressPercentage: Float) : DownloadState()

    object Downloaded : DownloadState()
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


sealed class AreaInfo {
    class EsriMapArea(val parentPortalItem: String, val preplannedArea: PreplannedMapArea) :
        AreaInfo()
}

sealed class AreaDownloadStatus {

    object Idle : AreaDownloadStatus()
    class InProgress(val progress: Int) : AreaDownloadStatus()

    class Aborted(
        val exception: Exception? = null,
        val message: String? = null
    ) : AreaDownloadStatus()

    object Completed : AreaDownloadStatus()
}