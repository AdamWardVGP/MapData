package com.award.mapdata.data

enum class MapType(val id: String) {
    TopLevelMap("TopLevelMap"),
    DownloadableMapArea("DownloadableMapArea"),
    Unknown("unknown");

    companion object {
        fun from(type: String): MapType {
            MapType.values().forEach {
                if(it.id == type) {
                    return it
                }
            }
            return Unknown
        }
    }
}
