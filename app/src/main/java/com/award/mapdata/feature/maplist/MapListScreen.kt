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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.award.mapdata.R
import com.award.mapdata.data.DownloadState
import com.award.mapdata.data.MapDataPreviewParamProvider
import com.award.mapdata.data.MapItemListElement
import com.award.mapdata.data.MapPreviewData
import com.award.mapdata.data.ViewMapInfo
import com.award.mapdata.ui.theme.MapDataTheme

@Composable
fun MapItemList(
    mapListItems: List<MapItemListElement>,
) {
    LazyColumn() {
        items(mapListItems, contentType = { it.viewType }) {
            when (it) {
                MapItemListElement.Divider -> {
                    Divider()
                }
                is MapItemListElement.Header -> {
                    HeaderRow(title = it.title)
                }
                is MapItemListElement.MapElement -> {
                    MapRow(
                        mapInfo = it.mapInfo,
                        requestDelete = {  },
                        requestDownload = {  }) {
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 540, heightDp = 1200)
@Composable
fun PreviewSampleColumn(
    @PreviewParameter(MapDataPreviewParamProvider::class)
    sampleMapElements: List<MapItemListElement>
) {
    MapDataTheme {
        MapItemList(sampleMapElements)
    }
}

//region row elements

@Composable
fun HeaderRow(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(Color(0xFF212121)),
        modifier = Modifier
            .padding(27.dp, 22.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 540)
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
            .padding(horizontal = 5.dp)
            .height(1.dp)
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 540)
@Composable
fun DividerPreview() {
    MapDataTheme {
        Row(
            modifier = Modifier
                .height(10.dp)
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
            .padding(26.dp, 10.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(131.dp, 88.dp)
                .background(Color(0xFFC3DBF9))
        )
        Spacer(modifier = Modifier.size(11.dp))
        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = mapInfo.title,
                style = MaterialTheme.typography.headlineMedium.copy(Color(0xFF212121)),
                maxLines = 1
            )

            Text(
                text = mapInfo.description,
                style = MaterialTheme.typography.bodyMedium.copy(Color(0xFF212121)),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
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
                Spacer(modifier = Modifier.size(29.dp))
                //No-op - downloading not supported, no UI to be shown
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 540)
@Composable
fun previewMapRow() {
    Surface {
        MapRow(
            ViewMapInfo(
                title = "Explore Maine",
                description = "Maine is a state in the New England region of the northeast United States. Maine is the 12th smallest by area, the 9th least some more text that I can't see from the default preview",
                downloadState = DownloadState.Unavailable
            ),
            { }, { }, { }
        )
    }
}

//endregion