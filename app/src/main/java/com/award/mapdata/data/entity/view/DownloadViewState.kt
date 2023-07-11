package com.award.mapdata.data.entity.view

/**
 * Representation of a the availability of a map item to be downloaded, and it's current download
 * state for the view layer
 */
sealed class DownloadViewState {

    //Indicates the element cannot support downloading
    object Unavailable : DownloadViewState()

    // No download has been triggered for this item
    object Idle : DownloadViewState()

    // Download progress
    // @param progressPercentage [0..1] as a float for 0 -> 100% downloaded
    class Downloading(val progressPercentage: Float) : DownloadViewState()

    object Downloaded : DownloadViewState()
}