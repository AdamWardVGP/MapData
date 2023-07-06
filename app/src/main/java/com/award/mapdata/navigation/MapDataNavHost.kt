package com.award.mapdata.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.award.mapdata.feature.maplist.MapListScreen
import com.award.mapdata.feature.maplist.MapListViewModel
import com.award.mapdata.feature.mapview.MapDetailScreen
import com.award.mapdata.navigation.MapNavDestination.MapDetails
import com.award.mapdata.navigation.MapNavDestination.MapList


@Composable
fun MapDataNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MapList.route,
        modifier = modifier
    ) {
        composable(route = MapList.route) {
            //Gets a view model scoped to the composable nav stack!
            val viewModel = hiltViewModel<MapListViewModel>()
            MapListScreen(
                viewModel,
                openMapDetails = {
                    navController.navigate(MapDetails.route)
                }
            )
        }
        composable(route = MapDetails.route) {
            MapDetailScreen()
        }
    }
}