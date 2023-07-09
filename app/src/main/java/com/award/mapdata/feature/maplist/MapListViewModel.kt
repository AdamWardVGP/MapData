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

    //Locking mechanism to prevent race condition access modifying multiple downloads in the mapList
    private val mutex = Mutex()

    //TODO do we want to restore via a SavedStateHandle?

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

    fun triggerDownload(mapID: MapID) {
        //TODO inject dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            mapRepository.downloadMapArea(mapKey, mapID.itemKey)?.collectLatest { downloadState ->
                mutex.withLock {
                    val list = _mapListFlow.value
                    val updatedList = list.map {
                        if((it is MapElement) && it.mapInfo.itemId.itemKey == mapID.itemKey) {
                            val newDownloadState = when(downloadState) {
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
                                    DownloadState.Downloading((downloadState.progress.toFloat() / 100f))
                                }
                            }
                            MapElement(updateViewMapInfo(it.mapInfo, newDownloadState))
                        } else {
                            it
                        }
                    }
                    _mapListFlow.value = updatedList
                }
            }
        }
    }

    fun triggerDelete(mapID: MapID) {
        viewModelScope.launch(Dispatchers.IO) {
            if(mapRepository.deleteDownloadedMapArea(mapID.itemKey)) {
                //deletion passed update models
                mutex.withLock {
                    val list = _mapListFlow.value.map {
                        if((it is MapElement) && it.mapInfo.itemId.itemKey == mapID.itemKey) {
                            MapElement(updateViewMapInfo(it.mapInfo, DownloadState.Idle))
                        } else {
                            it
                        }
                    }
                    _mapListFlow.value = list
                }
            } else {
                //TODO throw some error
            }
        }
    }
}

fun updateViewMapInfo(mapInfo: ViewMapInfo, downloadState: DownloadState): ViewMapInfo {
    return ViewMapInfo(
        itemId = mapInfo.itemId,
        imageUri = mapInfo.imageUri,
        title = mapInfo.title,
        description = mapInfo.description,
        downloadState = downloadState
    )
}