package com.award.mapdata.data.esri

import android.util.Log
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.tasks.JobStatus
import com.arcgismaps.tasks.offlinemaptask.DownloadPreplannedOfflineMapParameters
import com.arcgismaps.tasks.offlinemaptask.OfflineMapTask
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.arcgismaps.tasks.offlinemaptask.PreplannedUpdateMode
import com.award.mapdata.data.base.DownloadableMapAreaSource
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.AreaInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class EsriNetworkedMapAreaSource @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") baseEndpointURL: String,
    @Named("DOWNLOAD_FILE") val downloadFile: File
) : DownloadableMapAreaSource<PreplannedMapArea>() {

    companion object {
        const val LOG_TAG = "EsriMapAreaDataSource";
    }

    private val portal = Portal(baseEndpointURL)

    override suspend fun getMapAreas(id: String): List<PreplannedMapArea>? {
        //TODO exception handling for invalid IDs (or malformed portal URI)
        val portalItem = PortalItem(portal, id)
        val offlineMapTask = OfflineMapTask(portalItem)
        return offlineMapTask.getPreplannedMapAreas().getOrNull()
    }

    //https://developers.arcgis.com/kotlin/api-reference/arcgis-maps-kotlin/com.arcgismaps.tasks.offlinemaptask/-offline-map-task/index.html#148835997%2FFunctions%2F1086730362
    override suspend fun downloadPreplannedArea(area: AreaInfo): Flow<AreaDownloadStatus> {

        downloadFile.also {
            when {
                it.mkdirs() -> Log.i(LOG_TAG, "Created directory for offline map in " + it.path)
                it.exists() -> Log.i(LOG_TAG, "Offline map directory already exists at " + it.path)
                else -> Log.e(LOG_TAG, "Error creating offline map directory at: " + it.path)
            }
        }

        //TODO: This all assumes the item isn't already downloaded
        // ALso unsure of the download state in case of an abort/failure
        // we should check for existing items, their validity, and add cleanup

        (area as? AreaInfo.EsriMapArea)?.let {
            val offlineMapTask = OfflineMapTask(area.preplannedArea.portalItem)
            val params = DownloadPreplannedOfflineMapParameters(preplannedMapArea = area.preplannedArea)
            params.updateMode = PreplannedUpdateMode.NoUpdates

            val task = offlineMapTask.createDownloadPreplannedOfflineMapJob(
                params, downloadDirectoryPath = downloadFile.path + File.separator + area.preplannedArea.portalItem.itemId
            )

            task.start()

            return task.progress.combine(task.status) { progressUpdate, taskUpdate ->
                when(taskUpdate) {
                    JobStatus.Canceling -> {
                        AreaDownloadStatus.Aborted
                    }
                    JobStatus.Failed -> {
                        //Should have a better way to propagate error states up
                        AreaDownloadStatus.Aborted
                    }
                    JobStatus.NotStarted -> {
                        AreaDownloadStatus.Idle
                    }
                    JobStatus.Paused -> {
                        AreaDownloadStatus.Idle
                    }
                    JobStatus.Started -> {
                        AreaDownloadStatus.InProgress(progressUpdate)
                    }
                    JobStatus.Succeeded -> {
                        AreaDownloadStatus.Completed
                    }
                }
            }
        }

        return flowOf(AreaDownloadStatus.Aborted)
    }

}