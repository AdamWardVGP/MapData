package com.award.mapdata.navigation

sealed class MapNavDestination(val route: String) {
    object MapList : MapNavDestination("maplist")
    object MapDetails : MapNavDestination("mapdetails")
}