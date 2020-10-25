package com.sizeofanton.mdbbackend.pojo

import com.google.gson.annotations.SerializedName

data class UserInfo
(
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String,
    @SerializedName("isAdmin") val isAdmin: Boolean,
    @SerializedName("isActive") val isActive: Boolean
)