package com.award.mapdata.data.esri

import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.tasks.offlinemaptask.OfflineMapTask
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.base.DownloadableMapAreaSource
import javax.inject.Inject
import javax.inject.Named

class EsriNetworkedMapAreaSource @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") baseEndpointURL: String
): DownloadableMapAreaSource<PreplannedMapArea>() {

    private val portal = Portal(baseEndpointURL)

    override suspend fun getMapAreas(id: String): List<PreplannedMapArea>? {
        //TODO exception handling for invalid IDs (or malformed portal URI)
        val portalItem = PortalItem(portal, id)
        val offlineMapTask = OfflineMapTask(portalItem)
        return offlineMapTask.getPreplannedMapAreas().getOrNull()
    }
}