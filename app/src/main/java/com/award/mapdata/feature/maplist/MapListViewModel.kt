package com.award.mapdata.feature.maplist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.award.mapdata.R
import com.award.mapdata.data.entity.MapID
import com.award.mapdata.feature.maplist.MapItemListElement.Divider
import com.award.mapdata.feature.maplist.MapItemListElement.Header
import com.award.mapdata.data.base.MapRepository
import com.award.mapdata.data.entity.RepositoryResult
import com.award.mapdata.data.entity.view.ViewMapInfo
import com.award.mapdata.feature.maplist.MapItemListElement.MapElement
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

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
@HiltViewModel
class MapListViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    // Titles currently hardcoded. Language config changes currently don't update our string res
    // nor do we perform fetches to the data layer to update any text from the server.
    private val webHeaderText = context.getString(R.string.web_map)
    private val mapAreaText = context.getString(R.string.map_area)
    private val mapKey = context.getString(R.string.page_launch_id)

    val mapListFlow = combine(
        getTopLevelFlow(), mapRepository.getMapAreas(mapKey)
    ) { topSection, bottomSection ->

        if (topSection.contains(MapItemListElement.Loading)) {
            //Top is loading just display top
            topSection
        } else if (bottomSection is RepositoryResult.Success) {
            topSection +
            listOf(Divider, Header(mapAreaText)) +
            bottomSection.payload.map { MapElement(it) }
        } else {
            topSection + listOf(Divider, MapItemListElement.Loading)
        }
    }

    private fun getTopLevelFlow() = flow {
        emit(listOf(MapItemListElement.Loading))

        when (val mapResult = mapRepository.getTopLevelMap(mapKey)) {
            is RepositoryResult.Failure -> {
                //TODO refactor to handle failures
                emit(mutableListOf())
            }

            is RepositoryResult.Success -> {
                emit(
                    mutableListOf(
                        Header(webHeaderText),
                        MapElement(mapResult.payload),
                    )
                )
            }
        }
    }

    fun triggerDownload(mapID: MapID) {
        //TODO inject dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            mapRepository.downloadMapArea(mapKey, mapID.mapId)
        }
    }

    fun triggerDelete(mapID: MapID) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!mapRepository.deleteDownloadedMapArea(mapID.mapId)) {
                //TODO throw some error
            }
        }
    }
}