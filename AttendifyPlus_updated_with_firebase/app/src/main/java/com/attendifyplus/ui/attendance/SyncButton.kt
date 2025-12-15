package com.attendifyplus.ui.attendance

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SuccessGreen

@Composable
fun SyncButton(
    syncState: SyncState,
    onSync: () -> Unit
) {
    val (icon, text, color) = when (syncState) {
        is SyncState.Loading -> Triple(Icons.Default.Sync, "Syncing...", PrimaryBlue)
        is SyncState.Success -> Triple(Icons.Default.CloudDone, "Synced", SuccessGreen)
        is SyncState.Error -> Triple(Icons.Default.CloudOff, "Sync Failed", Color.Red)
        else -> Triple(Icons.Default.Sync, "Sync Data", Color.White)
    }

    Surface(
        shape = androidx.compose.foundation.shape.CircleShape,
        color = Color.White.copy(alpha = 0.2f),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onSync, enabled = syncState is SyncState.Idle)
                .padding(horizontal = 16.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Sync",
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
