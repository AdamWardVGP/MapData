package com.award.mapdata.feature.mapview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.award.mapdata.data.base.MapRepository
import com.award.mapdata.data.entity.MapType
import com.award.mapdata.data.entity.view.RenderableResult
import com.award.mapdata.data.entity.RepositoryResult
import com.award.mapdata.navigation.MapDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapDetailViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _renderableResultFlow =
        MutableStateFlow<RepositoryResult<RenderableResult>?>(null)
    val renderableResultFlow: StateFlow<RepositoryResult<RenderableResult>?> = _renderableResultFlow

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