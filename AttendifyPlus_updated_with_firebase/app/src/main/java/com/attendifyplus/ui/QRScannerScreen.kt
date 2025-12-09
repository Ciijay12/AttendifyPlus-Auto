package com.attendifyplus.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun QRScannerScreen(onDecoded: (String)->Unit) {
    val context = LocalContext.current
    
    // This is just a placeholder UI component kept for reference or future use.
    // The actual scanning logic is in QRAttendanceScreen.kt
    // Suppressing unused parameter warnings by using them trivially if needed or acknowledging they are placeholders.
    
    // To avoid "unused" warning in a way that keeps the signature:
    val callback = onDecoded 
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview placeholder
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.2f)))

        // Overlay box
        Box(
            Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .background(Color.Transparent)
                .border(3.dp, Color.White, RoundedCornerShape(16.dp))
        )

        // Copy + Toast sample
        Button(
            onClick = {
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                callback("Sample Data") // Use the callback
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            Text("Copy last result")
        }
    }
}
