package com.award.mapdata.feature.mapview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.award.mapdata.data.MapRepository
import com.award.mapdata.data.MapType
import com.award.mapdata.data.MapType.DownloadableMapArea
import com.award.mapdata.data.MapType.TopLevelMap
import com.award.mapdata.data.RenderableResult
import com.award.mapdata.navigation.MapDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MapDetailViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _renderableResultFlow = MutableStateFlow<RenderableResult?>(null)
    val renderableResultFlow: StateFlow<RenderableResult?> = _renderableResultFlow

    private val argMapId = checkNotNull(savedStateHandle.get<String>(MapDetails.argMapID))
    private val argMapType = checkNotNull(savedStateHandle.get<String>(MapDetails.argMapType))

    init {
        updateRenderableMap(argMapType, argMapId)
    }

    private fun updateRenderableMap(type: String, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _renderableResultFlow.value = mapRepository.getRenderableMap(MapType.from(type), id)
        }
    }
}