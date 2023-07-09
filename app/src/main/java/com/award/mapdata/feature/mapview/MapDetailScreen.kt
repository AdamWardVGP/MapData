package com.award.mapdata.feature.mapview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.arcgismaps.mapping.view.MapView
import com.award.mapdata.data.RenderableResult

@Composable
fun MapDetailScreen(
    viewModel: MapDetailViewModel
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapRenderData by viewModel.renderableResultFlow.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFC3DBF9))
    ) {
        //Dynamic renderer selection based on map provider source
        (mapRenderData as? RenderableResult.ArcGisMap)?.let { it ->
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    MapView(context)
                },
                update = { view ->
                    lifecycle.addObserver(view)
                    view.map = it.map
                }
            )
        }
    }

}