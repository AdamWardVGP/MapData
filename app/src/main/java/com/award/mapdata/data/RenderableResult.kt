package com.award.mapdata.data

import com.arcgismaps.mapping.ArcGISMap

sealed class RenderableResult() {
    class ArcGisMap(val map: ArcGISMap): RenderableResult()
    object Error : RenderableResult()
}
