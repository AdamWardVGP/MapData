package com.award.mapdata.data.esri

import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.portal.PortalQueryParameters
import com.award.mapdata.data.base.MapDataSource
import javax.inject.Inject
import javax.inject.Named

class EsriNetworkedMapDataSource @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") baseEndpointURL: String
): MapDataSource<PortalItem>() {

    private val portal = Portal(baseEndpointURL)

    override suspend fun getMapData(id: String): PortalItem? {
        //TODO exception handling for invalid IDs & errors
        val items = portal.findItems(PortalQueryParameters.item(id))
        return items.getOrNull()?.results?.first()
    }
}