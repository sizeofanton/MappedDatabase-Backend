package com.sizeofanton.mdbbackend.pojo

import com.sizeofanton.mdbbackend.localization.Language

data class User(
    val name: String,
    val token: String,
    var firebaseToken: String,
    var language: Language,
    var tokenLastUsage: Long
)