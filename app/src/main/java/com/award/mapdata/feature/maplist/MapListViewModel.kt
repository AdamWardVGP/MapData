package com.award.mapdata.feature.maplist

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.award.mapdata.R
import com.award.mapdata.data.entity.MapID
import com.award.mapdata.data.entity.MapItemListElement
import com.award.mapdata.data.entity.MapItemListElement.Divider
import com.award.mapdata.data.entity.MapItemListElement.Header
import com.award.mapdata.data.MapRepository
import com.award.mapdata.data.entity.AreaDownloadStatus
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapItemListElement.MapElement
import com.award.mapdata.data.entity.ViewMapInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class MapListViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    @ApplicationContext context: Context)
    : ViewModel() {

    private val _mapListFlow = MutableStateFlow<List<MapItemListElement>>(listOf())
    val mapListFlow: MutableStateFlow<List<MapItemListElement>> = _mapListFlow

    // Titles currently hardcoded. Language config changes currently don't update our string res
    // nor do we perform fetches to the data layer to update any text from the server.
    private val webHeaderText = context.getString(R.string.web_map)
    private val mapAreaText = context.getString(R.string.map_area)
    private val mapKey = context.getString(R.string.page_launch_id)

    init {
        populateList()
    }

    /**
     * Our map data app currently displays a screen for a fixed id: Explore Maine
     *
     * The output list contains
     * - A fixed header
     * - An Esri "Web Map"
     * - Divider
     * - Secondary header for "Preplanned" areas
     * - then a section of Esri "Portal Item"
     *
     * Data layers will provide the content for these items, however we must still
     * merge them into the desired ordered list output
     */
    private fun populateList() {
        viewModelScope.launch(Dispatchers.IO) {
            _mapListFlow.value = listOf(MapItemListElement.Loading)

            val list: MutableList<MapItemListElement> = getTopLevelMap()
                ?: //TODO: render some error instead
                return@launch

            //temp update to loading, and remove element after
            list.add(MapItemListElement.Loading)
            _mapListFlow.value = list

            var updatedList = list.subList(0, list.size - 2)
            updatedList = getDownloadableItems(updatedList)

            if(updatedList == null) {
                //TODO: render some error
                return@launch
            }

            _mapListFlow.value = updatedList
        }
    }

    private suspend fun getTopLevelMap(): MutableList<MapItemListElement>? {
        return mapRepository.getTopLevelMap(mapKey)?.let { mapItem ->
            mutableListOf(
                Header(webHeaderText),
                MapElement(mapItem),
                Divider
            )
        }
    }

    private suspend fun getDownloadableItems(list: MutableList<MapItemListElement>):
            MutableList<MapItemListElement> {
        val areas = mapRepository.getMapAreas(mapKey)
        list.add(Header(mapAreaText))
        areas?.forEach {
            list.add(MapElement(it))
        }
        return list
    }

    val mutex = Mutex()

    fun triggerDownload(mapID: MapID) {
        //TODO inject dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            mapRepository.downloadMapArea(mapKey, mapID.itemKey)?.collectLatest { downloadState ->
                mutex.withLock {
                    val list = _mapListFlow.value
                    list.firstOrNull() {
                        (it as? MapElement)?.mapInfo?.itemId?.itemKey == mapID.itemKey
                    }?.let { item ->
                        if(item is MapElement) {
                            val newItem = MapElement(
                                ViewMapInfo(
                                    itemId = item.mapInfo.itemId,
                                    imageUri = item.mapInfo.imageUri,
                                    title = item.mapInfo.title,
                                    description = item.mapInfo.description,
                                    downloadState = when(downloadState) {
                                        is AreaDownloadStatus.Aborted ->  {
                                            DownloadState.Idle
                                        }
                                        is AreaDownloadStatus.Completed ->  {
                                            DownloadState.Downloaded
                                        }
                                        is AreaDownloadStatus.Idle -> {
                                            DownloadState.Idle
                                        }
                                        is AreaDownloadStatus.InProgress -> {
                                            Log.v("AdamTest", "download state is now ${downloadState.progress}")
                                            DownloadState.Downloading((downloadState.progress.toFloat() / 100f))
                                        }
                                    }
                                )
                            )



                            //TODO ideally we shouldn't update the whole list.  We just want to insert a new item
                            //and this does a bunch of list copies.
                            val updatedList = list.map {
                                if((it as? MapElement)?.mapInfo?.itemId?.itemKey == mapID.itemKey){
                                    newItem
                                } else {
                                    it
                                }
                            }

                            _mapListFlow.value = updatedList
                        }
                    }
                }
            }
        }
    }

    fun triggerDelete(mapID: MapID) {

    }
}