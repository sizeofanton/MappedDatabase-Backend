package com.sizeofanton.mdbbackend.pojo

import com.google.gson.annotations.SerializedName

enum class EventType {
    ITEM_ADDED,
    ITEM_REMOVED,
    ITEM_EDITED,
    LOCATION_ADDED,
    LOCATION_REMOVED,
    LOCATION_STATUS_CHANGED
}

data class Event(
    @SerializedName("type") val type: EventType,
    @SerializedName("data") val data: List<String?>
)
