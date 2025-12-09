package com.attendifyplus.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.StandardScreen
import com.attendifyplus.ui.theme.SuccessGreen
import com.attendifyplus.ui.theme.WarningYellow
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentHistoryScreen(
    navController: NavController, 
    viewModel: StudentHistoryViewModel = getViewModel(),
    onLogout: () -> Unit = {}
) {
    val history by viewModel.history.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Stats
    val total = history.size
    val present = history.count { it.status.equals("present", ignoreCase = true) }
    val rate = if (total > 0) (present.toFloat() / total.toFloat() * 100).toInt() else 0

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Log Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
            text = { Text("Are you sure you want to log out?", color = MaterialTheme.colors.onSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = MaterialTheme.shapes.medium,
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    // Removed StandardScreen wrapper to use direct Surface with statusBarsPadding
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp) // Consistent padding
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Attendance History",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Stats Card
            Card(
                backgroundColor = PrimaryBlue,
                contentColor = Color.White,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Attendance Rate",
                            style = MaterialTheme.typography.caption.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                        Text(
                            text = "$rate%",
                            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Divider(
                        color = Color.White.copy(alpha = 0.2f), 
                        modifier = Modifier.height(40.dp).width(1.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Classes",
                            style = MaterialTheme.typography.caption.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                        Text(
                            text = "$total",
                            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No attendance records found.", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    items(history) { item ->
                        // Direct formatting without remember to avoid issues
                        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.timestamp))
                        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
                        
                        val statusColor = when(item.status.lowercase()) {
                            "present" -> SuccessGreen
                            "late" -> WarningYellow
                            "absent" -> Color.Red
                            else -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        }

                        Card(
                            shape = MaterialTheme.shapes.medium,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = statusColor.copy(alpha = 0.1f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (item.status == "present") Icons.Default.CheckCircle else Icons.Default.Schedule, 
                                            contentDescription = null, 
                                            tint = statusColor
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                                    )
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                                    )
                                }
                                
                                Surface(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                                    color = statusColor.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = item.status.replaceFirstChar { it.uppercase() },
                                        color = statusColor,
                                        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
