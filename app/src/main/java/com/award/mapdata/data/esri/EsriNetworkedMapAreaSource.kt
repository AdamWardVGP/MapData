package com.award.mapdata.data.esri

import android.util.Log
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.MobileMapPackage
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.tasks.JobStatus
import com.arcgismaps.tasks.offlinemaptask.DownloadPreplannedOfflineMapJob
import com.arcgismaps.tasks.offlinemaptask.DownloadPreplannedOfflineMapParameters
import com.arcgismaps.tasks.offlinemaptask.OfflineMapTask
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.arcgismaps.tasks.offlinemaptask.PreplannedUpdateMode
import com.award.mapdata.data.base.DownloadableMapAreaSource
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.AreaInfo
import com.award.mapdata.data.entity.RepositoryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        const val LOG_TAG = "EsriMapAreaDataSource"
    }

    //Active jobs are stored should another listener come in the future wanting status on an
    //in-progress download, or if we need to cancel
    private val activeDownloadTasks =
        mutableMapOf<String, DownloadPreplannedOfflineMapJob>()

    private val portal = Portal(baseEndpointURL)

    val context = CoroutineScope(Dispatchers.IO)

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

    /**
     * Begins a download job for the given map area.
     *
     * Esri Ref:
     * https://developers.arcgis.com/kotlin/api-reference/arcgis-maps-kotlin/com.arcgismaps.tasks.offlinemaptask/-offline-map-task/index.html#148835997%2FFunctions%2F1086730362
     */
    override suspend fun downloadPreplannedArea(area: AreaInfo): Flow<AreaDownloadStatus> {

        //TODO: Download stalling (without error) is a common occurring issue. SDK does not
        // give us any indication of failure via any of the flows.
        //
        // We either need a mechanism to detect error, abort downloads ourselves (timeout?) and also
        // a mechanism for checking if a download is actually valid since we're ending up with corrupted
        // files.

        if (area !is AreaInfo.EsriMapArea) {
            val errorTxt = "Provided map area is not supported by this handler"
            Log.e(LOG_TAG, errorTxt)
            return flowOf(
                AreaDownloadStatus.Aborted(
                    message = errorTxt
                )
            )
        }

        val itemId = area.preplannedArea.portalItem.itemId

        //Already downloaded items are ignored
        if(isAreaDownloaded(areaId = itemId)) {
            Log.i(LOG_TAG, "Map area already downloaded")
            return flowOf(AreaDownloadStatus.Completed)
        }

        //TODO wrap this all with a mutex to be sure 2 callers arent racing to kick off new download
        // tasks for the same item
        //already in progress jobs for a given item just get a back a flow to the existing job
        activeDownloadTasks[itemId]?.let {
            Log.i(LOG_TAG, "Current in progress download exists")
            return getFlowForTask(itemId, task = it)
        }

        if(!createDownloadDir())
            return flowOf(AreaDownloadStatus.Aborted(message = "Unable to create directory on file system"))

        val offlineMapTask = OfflineMapTask(PortalItem(portal, area.parentPortalItem))
        val params = DownloadPreplannedOfflineMapParameters(preplannedMapArea = area.preplannedArea)
        params.updateMode = PreplannedUpdateMode.NoUpdates

        val task = offlineMapTask.createDownloadPreplannedOfflineMapJob(
            params,
            downloadDirectoryPath = getFilePathForId(itemId)
        )

        activeDownloadTasks[itemId] = task

        task.start()

        return getFlowForTask(itemId, task)
    }

    private fun createDownloadDir(): Boolean {
        downloadFile.also {
            when {
                it.mkdirs() -> Log.i(LOG_TAG, "Created directory for offline map in " + it.path)
                it.exists() -> Log.i(LOG_TAG, "Offline map directory already exists at " + it.path)
                else -> {
                    Log.e(LOG_TAG, "Error creating offline map directory at: " + it.path)
                    return false
                }
            }
        }
        return true
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

    private suspend fun cancelInProgressJob(itemId: String) {
        Log.i(LOG_TAG, "Canceling active job $itemId")
        val job = activeDownloadTasks[itemId]
        activeDownloadTasks.remove(itemId)
        //TODO how to handle failed cancellations?
        job?.cancel()
        deletePreplannedArea(itemId)
    }


    override suspend fun cancelRunningDownloads() {
        Log.v(LOG_TAG, "active tasks = ${activeDownloadTasks.size}")
        activeDownloadTasks.keys.forEach {
            cancelInProgressJob(it)
        }
    }

    private fun getFilePathForId(itemId: String): String {
        return downloadFile.path + File.separator + itemId
    }

    private fun getFlowForTask(itemId: String, task: DownloadPreplannedOfflineMapJob): Flow<AreaDownloadStatus> {

        return combine(task.progress, task.status, task.messages) { progressUpdate, jobStatus, message ->

            Log.v(LOG_TAG, "Flow for Download [$itemId]\n+" +
                    "Progress: $progressUpdate\n" +
                    "JobStatus: $jobStatus\n" +
                    "lastMessage: ${message.message}\n")

            when (jobStatus) {
                JobStatus.Canceling -> {
                    AreaDownloadStatus.Aborted(message = "Canceling")
                }

                JobStatus.Failed -> {
                    Log.v(LOG_TAG, "Job failed, removing task $itemId")
                    //JobStatus doesn't contain error info to help debug, nor have I encountered
                    //useful errors in the message object for stalled downloads which don't send
                    //fail states
                    activeDownloadTasks.remove(itemId)
                    AreaDownloadStatus.Aborted(message = "Failure")
                }

                JobStatus.NotStarted -> {
                    AreaDownloadStatus.Idle
                }

                JobStatus.Paused -> {
                    AreaDownloadStatus.InProgress(progressUpdate)
                }

                JobStatus.Started -> {
                    AreaDownloadStatus.InProgress(progressUpdate)
                }

                JobStatus.Succeeded -> {
                    Log.v(LOG_TAG, "Job Succeeded, removing task $itemId")
                    activeDownloadTasks.remove(itemId)
                    AreaDownloadStatus.Completed
                }
            }
        }
    }

}