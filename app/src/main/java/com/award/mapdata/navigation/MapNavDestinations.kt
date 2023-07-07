package com.award.mapdata.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument


interface MapNavDestination {
    val route: String
}
object MapList : MapNavDestination {
    override val route = "maplist"
}
object MapDetails: MapNavDestination {
    override val route = "map_details"

    const val argMapID = "map_id"
    val routeWithArgs = "${route}/{${argMapID}}"

    val arguments = listOf(
        navArgument(argMapID) {
            type = NavType.StringType
            nullable = false
        }
    )

    fun getRouteForId(mapId: String): String {
        return "${route}/$mapId"
    }
}