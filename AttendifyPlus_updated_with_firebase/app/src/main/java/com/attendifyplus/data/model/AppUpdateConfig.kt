package com.attendifyplus.data.model

/**
 * Data class representing the update configuration in Firebase.
 * Using 'var' and default values to ensure Firebase can correctly parse the data.
 */
data class AppUpdateConfig(
    var versionCode: Int = 0,
    var versionName: String = "",
    var downloadUrl: String = "",
    var releaseNotes: String = "",
    var forceUpdate: Boolean = false
)
