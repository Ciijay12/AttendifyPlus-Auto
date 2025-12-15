package com.attendifyplus.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.attendifyplus.ui.attendance.DashboardViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun GlobalUpdateHandler(
    viewModel: DashboardViewModel = getViewModel()
) {
    val updateConfig by viewModel.updateConfig.collectAsState()

    if (updateConfig != null) {
        val config = updateConfig!!
        AlertDialog(
            onDismissRequest = { 
                // If forced, don't allow dismiss
                if (!config.forceUpdate) {
                    // We need a way to clear the config or ignore it for session
                    // For now, simple dismiss doesn't clear the flow, so it might reappear.
                    // Ideally we add a 'dismissUpdate' to VM. 
                }
            },
            title = { Text("Update Available") },
            text = { 
                Text("A new version (${config.versionName}) is available.\n\n${config.releaseNotes}") 
            },
            confirmButton = {
                TextButton(onClick = { viewModel.downloadUpdate(config.downloadUrl) }) {
                    Text("Update Now", color = Color.Blue)
                }
            },
            dismissButton = {
                if (!config.forceUpdate) {
                    TextButton(onClick = { /* Handle dismiss */ }) {
                        Text("Later")
                    }
                }
            }
        )
    }
}
