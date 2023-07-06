package com.award.mapdata.data

import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.tasks.offlinemaptask.OfflineMapTask
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea

class NetworkedMapDataSource constructor(baseEndpointURL: String): MapDataSource<PreplannedMapArea>() {

    private val portal = Portal(baseEndpointURL)

    override suspend fun getMapData(resourceId: String): List<PreplannedMapArea>? {

        val portalItem = PortalItem(portal, resourceId)
        val offlineMapTask = OfflineMapTask(portalItem)
        val preplannedMapAreasFuture = offlineMapTask.getPreplannedMapAreas()

        return preplannedMapAreasFuture.getOrNull()
    }
}