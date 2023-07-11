package com.award.mapdata.data.entity

/**
 * Internal download state relevant to repository and data layers
 */
sealed class AreaDownloadStatus {

    object Idle : AreaDownloadStatus()

    object Starting: AreaDownloadStatus()

    class InProgress(val progress: Int) : AreaDownloadStatus()

    class Aborted(
        val exception: Exception? = null,
        val message: String? = null
    ) : AreaDownloadStatus()

    object Completed : AreaDownloadStatus()
}