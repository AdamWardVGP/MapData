package com.award.mapdata.data.entity.view

import com.arcgismaps.mapping.ArcGISMap

/**
 * Generic wrapper around map contents to be rendered.
 */
sealed class RenderableResult {
    class ArcGisMap(val map: ArcGISMap) : RenderableResult()
}
