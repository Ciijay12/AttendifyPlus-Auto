package com.attendifyplus.data.model

import java.util.Date

data class SchoolEvent(
    val id: String,
    val title: String,
    val description: String?,
    val date: Date,
    val createdBy: String // e.g., Admin ID
)