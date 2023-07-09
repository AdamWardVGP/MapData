package com.award.mapdata.data.entity

enum class MapType(val typeKey: String) {
    Remote("RemoteMap"),
    LocalStorage("LocalStorageMap"),
    Unknown("unknown");

    companion object {
        fun from(typeKey: String): MapType {
            MapType.values().forEach {
                if (it.typeKey == typeKey) {
                    return it
                }
            }
            return Unknown
        }
    }
}
