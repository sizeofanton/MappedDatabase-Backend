package com.sizeofanton.mdbbackend.pojo

import com.google.gson.annotations.SerializedName

data class LocationInfo
(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("color") val color: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)