package com.award.mapdata

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.award.mapdata.data.entity.DownloadState
import com.award.mapdata.data.entity.MapItemListElement
import com.award.mapdata.data.mock.MapPreviewData.downloadedMapInfoSample
import com.award.mapdata.data.mock.MapPreviewData.downloadingMapInfoSample
import com.award.mapdata.data.mock.MapPreviewData.idleDownloadMapInfoSample
import com.award.mapdata.data.mock.MapPreviewData.unavailableDownloadMapInfoSample
import com.award.mapdata.feature.maplist.MapItemList
import org.junit.Rule
import org.junit.Test

/**
 * Multi-function test checking proper content conversion between MapItemListElement and composables
 * output into the LazyColumn, as well as validating the expected elements in those composables
 */
class MapListTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun checkExpectedContents_whenRowIsAHeader() {
        val testTitle = "Test Title"
        composeRule.setContent {
            MapItemList(
                mapListItems = listOf(MapItemListElement.Header(testTitle)),
                { },
                { },
                { }
            )
        }

        composeRule.onNode(hasText(testTitle)).assertExists()
    }

    @Test
    fun checkExpectedContents_whenRowIsADivider() {
        composeRule.setContent {
            MapItemList(
                mapListItems = listOf(MapItemListElement.Divider),
                { },
                { },
                { }
            )
        }

        composeRule.onNode(hasTestTag("divider")).assertExists()
    }

    @Test
    fun checkExpectedContents_whenRowIsNotDownloadable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sampleData = unavailableDownloadMapInfoSample

        composeRule.setContent {
            MapItemList(
                mapListItems = listOf(MapItemListElement.MapElement(sampleData)),
                { },
                { },
                { }
            )
        }

        composeRule.onNode(hasContentDescription(context.getString(R.string.download)))
            .assertDoesNotExist()
        composeRule.onNode(hasTestTag("progress_indicator")).assertDoesNotExist()
        composeRule.onNode(hasContentDescription(context.getString(R.string.delete)))
            .assertDoesNotExist()

        composeRule.onNode(hasText(unavailableDownloadMapInfoSample.title)).assertExists()
        composeRule.onNode(hasText(unavailableDownloadMapInfoSample.description)).assertExists()
    }

    @Test
    fun checkExpectedContents_whenRowIsIdle() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sampleData = idleDownloadMapInfoSample

        composeRule.setContent {
            MapItemList(
                mapListItems = listOf(MapItemListElement.MapElement(sampleData)),
                { },
                { },
                { }
            )
        }

        composeRule.onNode(hasContentDescription(context.getString(R.string.download)))
            .assertExists()
        composeRule.onNode(hasTestTag("progress_indicator")).assertDoesNotExist()
        composeRule.onNode(hasContentDescription(context.getString(R.string.delete)))
            .assertDoesNotExist()

        composeRule.onNode(hasText(idleDownloadMapInfoSample.title)).assertExists()
        composeRule.onNode(hasText(idleDownloadMapInfoSample.description)).assertExists()
    }

    @Test
    fun checkExpectedContents_whenRowIsDownloading() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sampleData = downloadingMapInfoSample
        val downloadInfo = downloadingMapInfoSample.downloadState as DownloadState.Downloading

        composeRule.setContent {
            MapItemList(
                mapListItems = listOf(MapItemListElement.MapElement(sampleData)),
                { },
                { },
                { }
            )
        }

        composeRule.onNode(hasContentDescription(context.getString(R.string.download)))
            .assertDoesNotExist()
        composeRule
            .onNode(
                hasProgressBarRangeInfo(
                    ProgressBarRangeInfo(downloadInfo.progressPercentage, 0f..1f)
                )
            )
            .assertExists()
        composeRule.onNode(hasContentDescription(context.getString(R.string.delete)))
            .assertDoesNotExist()

        composeRule.onNode(hasText(downloadingMapInfoSample.title)).assertExists()
        composeRule.onNode(hasText(downloadingMapInfoSample.description)).assertExists()
    }

    @Test
    fun checkExpectedContents_whenRowIsDownloaded() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sampleData = downloadedMapInfoSample

        composeRule.setContent {
            MapItemList(
                mapListItems = listOf(MapItemListElement.MapElement(sampleData)),
                { },
                { },
                { }
            )
        }

        composeRule.onNode(hasContentDescription(context.getString(R.string.download)))
            .assertDoesNotExist()
        composeRule.onNode(hasTestTag("progress_indicator")).assertDoesNotExist()
        composeRule.onNode(hasContentDescription(context.getString(R.string.delete))).assertExists()

        composeRule.onNode(hasText(downloadedMapInfoSample.title)).assertExists()
        composeRule.onNode(hasText(downloadedMapInfoSample.description)).assertExists()
    }

}