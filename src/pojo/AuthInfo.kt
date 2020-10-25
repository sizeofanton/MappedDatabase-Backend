package com.sizeofanton.mdbbackend.pojo

import com.google.gson.annotations.SerializedName

data class AuthInfo(
    @SerializedName("token") val token: String,
    @SerializedName("isAdmin") val isAdmin: Boolean
)