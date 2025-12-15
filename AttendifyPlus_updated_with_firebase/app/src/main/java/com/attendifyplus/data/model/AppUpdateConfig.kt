package com.attendifyplus.data.model

data class AppUpdateConfig(
    val versionCode: Int = 0,
    val versionName: String = "",
    val downloadUrl: String = "",
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false
)
