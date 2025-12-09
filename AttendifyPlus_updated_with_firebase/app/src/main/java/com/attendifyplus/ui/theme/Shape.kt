package com.attendifyplus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),  // Softer corners for smaller elements
    medium = RoundedCornerShape(16.dp), // Standard cards and modals
    large = RoundedCornerShape(24.dp)   // Bottom sheets, larger modals
)

// Extra shapes not in standard Material set
val PillShape = RoundedCornerShape(50) // Fully rounded for buttons
