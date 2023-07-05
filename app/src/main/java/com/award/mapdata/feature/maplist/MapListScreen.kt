package com.award.mapdata.feature.maplist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.award.mapdata.R
import com.award.mapdata.data.DownloadState
import com.award.mapdata.data.MapItemListElement
import com.award.mapdata.data.ViewMapInfo
import com.award.mapdata.ui.theme.MapDataTheme

@Composable
fun mapItemList(
    mapListItems: List<MapItemListElement>,
) {
    LazyColumn() {
        items(mapListItems, contentType = { it.viewType }) {
            when (it) {
                MapItemListElement.Divider -> TODO()
                is MapItemListElement.Header -> TODO()
                is MapItemListElement.MapElement -> TODO()
            }
        }
    }
}

//region row elements

@Composable
fun HeaderRow(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(54.dp, 45.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 1080)
@Composable
fun HeaderPreview() {
    MapDataTheme {
        HeaderRow(stringResource(R.string.web_map))
    }
}

@Composable
fun Divider() {
    Box(
        Modifier
            .padding(horizontal = 10.dp)
            .height(1.dp)
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 1080)
@Composable
fun DividerPreview() {
    MapDataTheme {
        Row(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider()
        }
    }
}

@Composable
fun MapRow(
    mapInfo: ViewMapInfo,
    requestDelete: () -> Unit,
    requestDownload: () -> Unit,
    onMapInfoSelected: () -> Unit
) {

    Row(
        modifier = Modifier
            .padding(52.dp, 21.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(262.dp, 176.dp)
                .background(Color(0xFFC3DBF9))
        )
        Spacer(modifier = Modifier.size(23.dp))
        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = mapInfo.title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1
            )

            Text(
                text = mapInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
        when (mapInfo.downloadState) {
            is DownloadState.Downloaded -> {
                //download is complete, allow deleting map
            }

            is DownloadState.Downloading -> {
                //download is in progress, update progress indicator

            }

            is DownloadState.Idle -> {
                //download available requires rendering download asset
            }
            is DownloadState.Unavailable -> {
                Spacer(modifier = Modifier.size(58.dp))
                //No-op - downloading not supported, no UI to be shown
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 1080) //x 2400
@Composable
fun previewMapRow() {
    Surface {
        MapRow(
            ViewMapInfo(
                title = "test",
                description = "description text that can go on for awhile. This text is pretty long and should truncate itself so that it's only a few lines max and doesnt fill up the screen unneccisarily",
                downloadState = DownloadState.Unavailable
            ),
            { }, { }, { }
        )
    }
}

//endregion