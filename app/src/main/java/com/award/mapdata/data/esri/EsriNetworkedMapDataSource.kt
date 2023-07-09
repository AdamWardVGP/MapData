package com.award.mapdata.data.esri

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.portal.PortalQueryParameters
import com.award.mapdata.data.base.MapDataSource
import com.award.mapdata.data.entity.RepositoryResult
import javax.inject.Inject
import javax.inject.Named

class EsriNetworkedMapDataSource @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") baseEndpointURL: String
) : MapDataSource<PortalItem, ArcGISMap>() {

    private val portal = Portal(baseEndpointURL)

    override suspend fun getMapData(id: String): RepositoryResult<PortalItem> {
        return try {
            val result = portal.findItems(PortalQueryParameters.item(id)).getOrThrow()
            RepositoryResult.Success(result.results.first())
        } catch (ex: Exception) {
            RepositoryResult.Failure(ex, "Failed retrieving map data")
        }
    }

    override fun getRenderableMap(id: String): RepositoryResult<ArcGISMap> {
        val portalItem = PortalItem(portal, id)
        return RepositoryResult.Success(ArcGISMap(portalItem))
    }
}