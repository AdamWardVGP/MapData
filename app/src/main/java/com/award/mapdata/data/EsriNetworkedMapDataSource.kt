package com.award.mapdata.data

import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.tasks.offlinemaptask.OfflineMapTask
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import javax.inject.Inject
import javax.inject.Named

class EsriNetworkedMapDataSource @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") baseEndpointURL: String
): MapDataSource<PreplannedMapArea>() {

    private val portal = Portal(baseEndpointURL)

    override suspend fun getMapData(id: String): List<PreplannedMapArea>? {
        //TODO exception handling for invalid IDs (or malformed portal URI)
        val portalItem = PortalItem(portal, id)
        val offlineMapTask = OfflineMapTask(portalItem)
        val preplannedMapAreasFuture = offlineMapTask.getPreplannedMapAreas()

        return preplannedMapAreasFuture.getOrNull()
    }
}