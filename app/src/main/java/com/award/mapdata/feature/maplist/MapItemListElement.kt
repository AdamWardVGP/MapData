package com.award.mapdata.feature.maplist

import com.award.mapdata.data.entity.view.ViewMapInfo

sealed class MapItemListElement(val viewType: Int) {
    class Header(val title: String) : MapItemListElement(1)
    class MapElement(val mapInfo: ViewMapInfo) : MapItemListElement(2)
    object Divider : MapItemListElement(3)
    object Loading : MapItemListElement(4)
}