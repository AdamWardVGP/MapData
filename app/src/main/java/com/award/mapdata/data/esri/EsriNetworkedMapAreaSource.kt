package com.award.mapdata.data.esri

import android.util.Log
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
import com.award.mapdata.data.entity.RepositoryResult
import com.award.mapdata.data.entity.view.RenderableResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named

/**
 * This data source provides a few main functions for the repository layer:
 * - Access to a flow of preplanned areas for a given ID
 * - An ability to launch download jobs & cleanup previously download items
 * - Automatic updates to existing flows containing items with changing download states
 */
class EsriNetworkedMapAreaSource @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") baseEndpointURL: String,
    @Named("DOWNLOAD_FILE") val downloadFile: File
) : DownloadableMapAreaSource<PreplannedMapArea>() {

    companion object {
        const val LOG_TAG = "EsriMapAreaDataSource"
    }

    private val portal = Portal(baseEndpointURL)

    //Coroutines are scoped to supervisor to ensure cancellation of one job doesn't stop another
    private val supervisorJob = SupervisorJob()
    private val mapAreaScope = CoroutineScope(Dispatchers.Default + supervisorJob)

    private val activeDownloadTasks =
        mutableMapOf<String, DownloadPreplannedOfflineMapJob>()

    private var activeId: String = ""
    private var activeAreaFlow =
        MutableStateFlow(AreaItemsList<PreplannedMapArea>(null))

    override fun getMapAreas(id: String): StateFlow<AreaItemsList<PreplannedMapArea>> {

        if (activeId == id) {
            return activeAreaFlow
        }

        //singleton flow is starting a new fetch
        mapAreaScope.launch {

            try {
                val portalItem = PortalItem(portal, id)
                val offlineMapTask = OfflineMapTask(portalItem)
                val result = offlineMapTask.getPreplannedMapAreas().getOrThrow()
                val itemPairs = result.map {
                    val itemId = it.portalItem.itemId
                    val activeDownload = activeDownloadTasks[itemId]
                    it to if (isAreaDownloaded(it.portalItem.itemId)) {
                        AreaDownloadStatus.Completed
                    } else if (activeDownload != null) {
                        AreaDownloadStatus.InProgress(activeDownload.progress.value)
                    } else {
                        AreaDownloadStatus.Idle
                    }
                }

                activeId = id
                activeAreaFlow.tryEmit(AreaItemsList(RepositoryResult.Success(itemPairs)))
            } catch (ex: Exception) {

                activeId = ""
                activeAreaFlow.tryEmit(
                    AreaItemsList(
                        RepositoryResult.Failure(
                            ex,
                            "Failed to get Map Area"
                        )
                    )
                )
            }
        }

        return activeAreaFlow
    }

    override fun deletePreplannedArea(id: String): Boolean {
        val file = File(downloadFile.path + File.separator + id)
        if (file.exists()) {
            val deleteResult = file.deleteRecursively()

            if (deleteResult) {
                updateFlowWithItem(id, AreaDownloadStatus.Idle)
            }

            return deleteResult
        }
        return false
    }

    /**
     * Begins a download job for the given map area.
     *
     * Esri Ref:
     * https://developers.arcgis.com/kotlin/api-reference/arcgis-maps-kotlin/com.arcgismaps.tasks.offlinemaptask/-offline-map-task/index.html#148835997%2FFunctions%2F1086730362
     */
    override suspend fun downloadPreplannedArea(
        parentId: String,
        childId: String
    ): AreaDownloadStatus {

        //TODO: Download stalling (without error) is a common occurring issue. SDK does not
        // give us any indication of failure via any of the flows.
        //
        // We either need a mechanism to detect error, abort downloads ourselves (timeout?) and also
        // a mechanism for checking if a download is actually valid since we're ending up with corrupted
        // files.

        // TODO wrap this all with a mutex to be sure 2 callers arent racing to kick off new download
        // tasks for the same item

        //Already downloaded items are ignored
        if (isAreaDownloaded(areaId = childId)) {
            Log.v(LOG_TAG, "Map area already downloaded")
            return AreaDownloadStatus.Completed
        }


        // Since we're trying to limit the usage to a single active flow, we can only update and
        // download an item if it's in that flow.
        val activeTask = findItemWithId(activeAreaFlow.value.getItems(), childId)
        if (activeTask == null) {
            val error = "Item requesting download must be in current flow"
            Log.e(LOG_TAG, error)
            return AreaDownloadStatus.Aborted(message = error)
        }

        // Already in progress jobs for a given item just get a back a state to the existing task
        if (activeTask.second is AreaDownloadStatus.InProgress) {
            Log.v(LOG_TAG, "Already in progress, returning active download state")
            return activeTask.second
        }

        if (!createDownloadDir()) {
            val error = "Unable to create directory on file system"
            Log.e(LOG_TAG, error)
            return AreaDownloadStatus.Aborted(message = error)
        }

        val offlineMapTask = OfflineMapTask(PortalItem(portal, parentId))
        val params = DownloadPreplannedOfflineMapParameters(preplannedMapArea = activeTask.first)
        params.updateMode = PreplannedUpdateMode.NoUpdates

        val task = offlineMapTask.createDownloadPreplannedOfflineMapJob(
            params,
            downloadDirectoryPath = getFilePathForId(childId)
        )

        task.start()

        return launchFlowForTask(childId, task)
    }


    override suspend fun getRenderableMap(id: String): RepositoryResult<RenderableResult> {
        val file = File(getFilePathForId(id))
        return if (file.exists()) {
            val mapPack = MobileMapPackage(file.path)
            mapPack.load()
            mapPack.maps.firstOrNull()?.let {
                RepositoryResult.Success(RenderableResult.ArcGisMap(it))
            } ?: run {
                //assume we have a corrupted file and perform cleanup
                deletePreplannedArea(id)
                RepositoryResult.Failure(message = "Loading local map failed")
            }
        } else {
            RepositoryResult.Failure(message = "Local map was not found in file system")
        }
    }

    private suspend fun cancelInProgressJob(itemId: String) {
        //TODO how to handle failed cancellations?
        Log.i(LOG_TAG, "Canceling active job $itemId")
        val job = activeDownloadTasks[itemId]
        activeDownloadTasks.remove(itemId)
        job?.cancel()
        deletePreplannedArea(itemId)
    }


    override suspend fun cancelRunningDownloads() {
        Log.v(LOG_TAG, "active tasks = ${activeDownloadTasks.size}")
        activeDownloadTasks.keys.forEach {
            cancelInProgressJob(it)
        }
    }

    //region internal utils

    private fun getFilePathForId(itemId: String): String {
        return downloadFile.path + File.separator + itemId
    }

    /**
     * Returns a flow which
     */
    private suspend fun launchFlowForTask(
        itemId: String,
        task: DownloadPreplannedOfflineMapJob
    ): AreaDownloadStatus {

        activeDownloadTasks[itemId] = task
        mapAreaScope.launch {
            combine(
                task.progress,
                task.status,
                task.messages
            ) { progressUpdate, jobStatus, message ->

                Log.v(
                    LOG_TAG, "Flow for Download [$itemId]\n+" +
                            "Progress: $progressUpdate\n" +
                            "JobStatus: $jobStatus\n" +
                            "lastMessage: ${message.message}\n"
                )

                val result = when (jobStatus) {
                    JobStatus.Canceling -> {
                        supervisorJob
                        AreaDownloadStatus.Aborted(message = "Canceling")
                    }

                    JobStatus.Failed -> {
                        Log.v(LOG_TAG, "Job failed, removing task $itemId")
                        //JobStatus doesn't contain error info to help debug, nor have I encountered
                        //useful errors in the message object for stalled downloads which don't send
                        //fail states
                        this.coroutineContext.job.cancel()
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
                        this.coroutineContext.job.cancel()
                        AreaDownloadStatus.Completed
                    }
                }
                result
            }.collect { updatedStatus ->
                updateFlowWithItem(itemId, updatedStatus)
            }
        }

        return AreaDownloadStatus.Starting
    }


    /**
     * Utility for updating any currently active flows which might contain an area having updates
     * performed on it (download start / downloading / deletion)
     */
    private fun updateFlowWithItem(id: String, downloadState: AreaDownloadStatus) {
        val activeDownloads = activeAreaFlow.value.getItems()
        if (findItemWithId(activeDownloads, id) != null) {

            val updatedList = activeDownloads.map {
                if (it.first.portalItem.itemId == id) {
                    it.first to downloadState
                } else {
                    it
                }
            }
            activeAreaFlow.value = AreaItemsList(RepositoryResult.Success(updatedList))
        }
    }

    private fun findItemWithId(
        itemList: List<Pair<PreplannedMapArea, AreaDownloadStatus>>,
        id: String
    ): Pair<PreplannedMapArea, AreaDownloadStatus>? {
        return itemList.find { it.first.portalItem.itemId == id }
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

    private fun isAreaDownloaded(areaId: String): Boolean {
        return File(getFilePathForId(areaId)).exists()
    }

    //endregion

}