package com.award.mapdata.feature.mapview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.arcgismaps.mapping.view.MapView
import com.award.mapdata.data.entity.view.RenderableResult
import com.award.mapdata.data.entity.RepositoryResult

@Composable
fun MapDetailScreen(
    viewModel: MapDetailViewModel
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapRenderData by viewModel.renderableResultFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC3DBF9))
    ) {
        when(val renderDataState = mapRenderData) {
            is RepositoryResult.Failure -> {
                Text(text = "Unable to load map data")
            }
            is RepositoryResult.Success -> {
                if(renderDataState.payload is RenderableResult.ArcGisMap) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            MapView(context)
                        },
                        update = { view ->
                            lifecycle.addObserver(view)
                            view.map = renderDataState.payload.map
                        }
                    )
                }
            }
            null -> {
                //no-op content is loading
            }
        }
    }

}