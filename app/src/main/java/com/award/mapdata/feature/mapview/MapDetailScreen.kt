package com.award.mapdata.feature.mapview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.view.MapView
import com.arcgismaps.portal.Portal

@Composable
fun MapDetailScreen(
    viewModel: MapDetailViewModel,
    mapId: String = "41281c51f9de45edaf1c8ed44bb10e30"
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFC3DBF9))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context)
            },
            update = { view ->
                val portal = Portal(viewModel.esriUrlBase, Portal.Connection.Anonymous)
                val portalItem = PortalItem(portal, mapId)
                val map = ArcGISMap(portalItem)
                lifecycle.addObserver(view)
                view.map = map
            }
        )
    }

}