package com.award.mapdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.award.mapdata.navigation.MapDataNavHost
import com.award.mapdata.ui.theme.MapDataTheme
import dagger.hilt.android.AndroidEntryPoint

//import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapDataApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 540, heightDp = 1200)
@Composable
fun MapDataApp() {
    MapDataTheme {
        var navController = rememberNavController()

        val currentBackStack by navController.currentBackStackEntryAsState()
        val showBackButton by remember(currentBackStack) {
            derivedStateOf {
                navController.previousBackStackEntry != null
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color(0xFF5114DB),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = {
                        //TODO pages themselves should probably provide their titles instead.
                        Text(text = stringResource(id = R.string.title))
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    tint = Color.White,
                                    contentDescription = "Back"

                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            MapDataNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}