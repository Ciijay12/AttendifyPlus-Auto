package com.attendifyplus.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A standard screen container that centrally handles system insets (status bar) 
 * and applies the standard screen padding defined in Dimens.kt.
 * 
 * Use this wrapper for any secondary screen that requires a safe area (not covered by a custom header).
 */
@Composable
fun StandardScreen(
    backgroundColor: Color = MaterialTheme.colors.background, // Updated default to theme background
    usePadding: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize().then(modifier),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Always respect status bar
                .then(if (usePadding) Modifier.padding(Dimens.PaddingLarge) else Modifier) // Apply 24.dp from Dimens
        ) {
            content()
        }
    }
}
