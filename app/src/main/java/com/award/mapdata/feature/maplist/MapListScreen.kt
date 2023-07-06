package com.award.mapdata.feature.maplist

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.award.mapdata.R
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.mock.MapDataPreviewParamProvider
import com.award.mapdata.data.entity.MapItemListElement
import com.award.mapdata.data.mock.MapPreviewData
import com.award.mapdata.data.mock.MapPreviewData.unavailableDownloadMapInfoSample
import com.award.mapdata.data.entity.ViewMapInfo
import com.award.mapdata.ui.theme.MapDataTheme

@Composable
fun MapListScreen(openMapDetails: (ViewMapInfo) -> Unit) {
    //TODO hook up with viewmodel
    MapItemList(MapPreviewData.dataElements, openMapDetails)
}

@Composable
fun MapItemList(
    mapListItems: List<MapItemListElement>,
    openMapDetails: (ViewMapInfo) -> Unit
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
                        requestDownload = {  },
                        onMapInfoSelected = openMapDetails)
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
        MapItemList(sampleMapElements) { }
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
    Box(modifier = Modifier
        .padding(horizontal = 5.dp)
        .height(1.dp)
        .fillMaxWidth()
        .background(Color(0xFFE0E0E0))
        .testTag("divider")
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
    requestDelete: (ViewMapInfo) -> Unit,
    requestDownload: (ViewMapInfo) -> Unit,
    onMapInfoSelected: (ViewMapInfo) -> Unit
) {

    Row(
        modifier = Modifier
            .padding(26.dp, 10.dp)
            .wrapContentHeight()
            .fillMaxWidth()
            .clickable { onMapInfoSelected(mapInfo) },
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
                SpacedRowIcon(R.drawable.delete, mapInfo, requestDelete, R.string.delete)
            }

            is DownloadState.Downloading -> {
                Spacer(modifier = Modifier.size(26.dp))
                //download is in progress, update progress indicator
                CircularProgressIndicator(
                    progress = mapInfo.downloadState.progressPercentage,
                    color = Color(0xFF5114DB),
                    strokeWidth = 5.dp,
                    modifier = Modifier.testTag("progress_indicator")
                )
                Spacer(modifier = Modifier.size(23.dp))
            }

            is DownloadState.Idle -> {
                SpacedRowIcon(R.drawable.download, mapInfo, requestDownload, R.string.download)
            }
            is DownloadState.Unavailable -> {
                Spacer(modifier = Modifier.size(29.dp))
            }
        }
    }
}

@Composable
fun SpacedRowIcon(@DrawableRes iconRes: Int, mapInfo: ViewMapInfo,
                  clickHandler: (ViewMapInfo) -> Unit, @StringRes description: Int) {
    Spacer(modifier = Modifier.size(25.dp))
    Button(
        onClick = { clickHandler(mapInfo) },
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        contentPadding = PaddingValues(0.dp)) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = stringResource(id = description),
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp)
        )
    }
    Spacer(modifier = Modifier.size(14.dp))
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 540)
@Composable
fun previewMapRow() {
    Surface {
        MapRow(
            unavailableDownloadMapInfoSample,
            { }, { }, { }
        )
    }
}

//endregion