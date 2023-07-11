package com.award.mapdata.data.entity.view

import com.award.mapdata.data.entity.MapID

/**
 * Container for view layer rendering of map elements
 */
data class ViewMapInfo(
    val itemId: MapID,
    val imageUri: String?,
    val title: String,
    val description: String,
    val downloadViewState: DownloadViewState
)


