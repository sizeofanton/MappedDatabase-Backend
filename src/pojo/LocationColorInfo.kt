package com.sizeofanton.mdbbackend.pojo

import com.google.gson.annotations.SerializedName

data class LocationColorInfo
(
    @SerializedName("id") val id: Int,
    @SerializedName("color") val color: Int
)