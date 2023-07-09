package com.award.mapdata.data.entity

/**
 * Generalized IDs to ensure strong type matching across calls to data layer
 */
sealed class MapID(val mapType: String, val mapId: String) {
    class EsriID(mapType: MapType, mapId: String) : MapID(mapType.typeKey, mapId)
}