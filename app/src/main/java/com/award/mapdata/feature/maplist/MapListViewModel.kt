package com.award.mapdata.feature.maplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.award.mapdata.MapDataApplication
import com.award.mapdata.R
import com.award.mapdata.data.entity.MapID
import com.award.mapdata.data.entity.MapItemListElement
import com.award.mapdata.data.entity.MapItemListElement.Divider
import com.award.mapdata.data.entity.MapItemListElement.Header
import com.award.mapdata.data.MapRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

//@HiltViewModel
class MapListViewModel(private val mapRepository: MapRepository, application: MapDataApplication)
    : ViewModel() {

    //TODO Loading states?
    private val _mapListFlow = MutableStateFlow<List<MapItemListElement>>(listOf())
    private val mapListFlow: MutableStateFlow<List<MapItemListElement>> = _mapListFlow

    // Titles currently hardcoded. Language config changes currently don't update our string res
    // nor do we perform fetches to the data layer to update any text from the server.
    private val webHeaderText = application.getString(R.string.web_map)
    private val mapAreaText = application.getString(R.string.map_area)

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
        viewModelScope.launch {
            //TODO error handling
            val mapItems = mapRepository.getTopLevelMap("")
            val elementList = mutableListOf(
                Header(webHeaderText),
                //where is the top map item?
                Divider,
                Header(webHeaderText),
            )
            mapItems?.forEach {
                elementList.add(MapItemListElement.MapElement(it))
            }
            _mapListFlow.value = elementList
        }
    }

    fun triggerDownload(mapID: MapID) {

    }

    fun triggerDelete(mapID: MapID) {

    }
}