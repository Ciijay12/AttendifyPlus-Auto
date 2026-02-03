package com.attendifyplus.data.remote.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UpdateInfo(
    val versionCode: Int = 0,
    val versionName: String = "",
    val downloadUrl: String = "",
    val releaseNotes: String = ""
)
