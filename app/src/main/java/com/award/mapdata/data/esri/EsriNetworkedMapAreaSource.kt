package com.award.mapdata.data.esri

import android.util.Log
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.MobileMapPackage
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
import com.award.mapdata.data.entity.RepositoryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class EsriNetworkedMapAreaSource @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") baseEndpointURL: String,
    @Named("DOWNLOAD_FILE") val downloadFile: File
) : DownloadableMapAreaSource<PreplannedMapArea, ArcGISMap>() {

    companion object {
        const val LOG_TAG = "EsriMapAreaDataSource";
    }

    private val portal = Portal(baseEndpointURL)

    override suspend fun getMapAreas(id: String): RepositoryResult<List<PreplannedMapArea>> {
        return try {
            val portalItem = PortalItem(portal, id)
            val offlineMapTask = OfflineMapTask(portalItem)
            val result = offlineMapTask.getPreplannedMapAreas().getOrThrow()
            RepositoryResult.Success(result)
        } catch (ex: Exception) {
            RepositoryResult.Failure(ex, "Failed to get Map Area")
        }
    }

    override fun isAreaDownloaded(areaId: String): Boolean {
        return File(getFilePathForId(areaId)).exists()
    }

    override fun deletePreplannedArea(id: String): Boolean {
        val file = File(downloadFile.path + File.separator + id)
        if (file.exists()) {
            return file.deleteRecursively()
        }
        return false
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

        //TODO: This all assumes the item isn't already downloaded, or in progress downloading
        //
        // Also unsure of the download state in case of an abort/failure
        // we should check for existing items, their validity, and add cleanup
        //
        // Also these tasks are fired and forgotten about. We should retain active tasks for cancellation

        (area as? AreaInfo.EsriMapArea)?.let {
            val offlineMapTask = OfflineMapTask(
                PortalItem(portal, area.parentPortalItem)
            )

            val params =
                DownloadPreplannedOfflineMapParameters(preplannedMapArea = area.preplannedArea)
            params.updateMode = PreplannedUpdateMode.NoUpdates

            val task = offlineMapTask.createDownloadPreplannedOfflineMapJob(
                params,
                downloadDirectoryPath = getFilePathForId(area.preplannedArea.portalItem.itemId)
            )

            task.start()

            return task.progress.combine(task.status) { progressUpdate, taskUpdate ->
                when (taskUpdate) {
                    JobStatus.Canceling -> {
                        AreaDownloadStatus.Aborted(message = "Canceling")
                    }

                    JobStatus.Failed -> {
                        //TODO: JobStatus doesn't contain error info, need to find a way to propagate
                        // error states up
                        AreaDownloadStatus.Aborted(message = "failure")
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

        return flowOf(AreaDownloadStatus.Aborted(message = "Provided map area is not supported by this handler"))
    }


    override suspend fun getRenderableMap(id: String): RepositoryResult<ArcGISMap> {
        val file = File(getFilePathForId(id))
        return if (file.exists()) {
            val mapPack = MobileMapPackage(file.path)
            mapPack.load()
            mapPack.maps.firstOrNull()?.let {
                RepositoryResult.Success(it)
            } ?: RepositoryResult.Failure(message = "Loading local map failed")
        } else {
            RepositoryResult.Failure(message = "Local map was not found in file system")
        }
    }

    private fun getFilePathForId(itemId: String): String {
        return downloadFile.path + File.separator + itemId
    }

}