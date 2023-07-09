package com.award.mapdata.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument


sealed interface MapNavDestination {
    val route: String
}
object MapList : MapNavDestination {
    override val route = "maplist"
}
object MapDetails: MapNavDestination {
    override val route = "map_details"

    const val argMapID = "map_id"
    const val argMapType = "map_type"

    val routeWithArgs = "${route}/{${argMapType}}:{${argMapID}}"

    val arguments = listOf(
        navArgument(argMapType) {
            type = NavType.StringType
            nullable = false
        },
        navArgument(argMapID) {
            type = NavType.StringType
            nullable = false
        }
    )

    fun getRouteForId(mapType: String, mapId: String): String {
        return "${route}/$mapType:$mapId"
    }
}