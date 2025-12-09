package com.attendifyplus.ui.attendance

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SetSystemBarIcons(useDarkIcons: Boolean) {
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme()
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = if (isDarkTheme) false else useDarkIcons
        }
    }
}

@Composable
fun SetupPromptOverlay(onNavigate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = {}), // Consume clicks
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.EditCalendar,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Setup Required",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Please configure the academic calendar to enable attendance tracking and other features.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface)
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onNavigate) {
                    Text("Go to Settings", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.onSurface),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SyncStatusCard(
    unsyncedCount: Int, 
    lastSyncTime: Long = 0L,
    syncState: SyncState = SyncState.Idle, // Add syncState parameter
    onSyncNow: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (unsyncedCount > 0) Icons.Default.CloudOff else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (unsyncedCount > 0) Color.Red else Color(0xFF4CAF50), // Green
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                    )
                    Text(
                        text = if (unsyncedCount > 0) "$unsyncedCount records pending upload" else "All records synced",
                        style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                    if (lastSyncTime > 0) {
                        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                        Text(
                            text = "Last synced: ${sdf.format(Date(lastSyncTime))}",
                            style = MaterialTheme.typography.caption.copy(color = PrimaryBlue, fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Sync Now Button and State
            if (onSyncNow != null) {
                Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (syncState) {
                        is SyncState.Idle -> {
                            TextButton(
                                onClick = onSyncNow,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("SYNC NOW", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                        is SyncState.Loading -> {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .align(Alignment.BottomCenter),
                                color = PrimaryBlue
                            )
                            Text(
                                "Syncing...", 
                                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            )
                        }
                        is SyncState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Synced Successfully", style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = SuccessGreen))
                            }
                        }
                        is SyncState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sync Failed", style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = Color.Red))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        modifier = modifier
            .height(100.dp) // Adjusted height for compactness
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No classes scheduled for today.",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun StatusOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.1f) else MaterialTheme.colors.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp, 
            color = if (selected) color else MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = if (selected) color else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold, 
                    color = if (selected) color else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun CompactClassItem(subjectClass: SubjectClassEntity, onClick: () -> Unit) {
    val gradeInt = subjectClass.gradeLevel.toIntOrNull()
    val department = if (gradeInt != null && gradeInt >= 11) "SHS" else "JHS"
    val deptColor = if (department == "SHS") SecondaryTeal else PrimaryBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dept Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = deptColor.copy(alpha = 0.1f),
            modifier = Modifier.size(width = 48.dp, height = 48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = department,
                    style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = deptColor)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subjectClass.subjectName,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface),
                maxLines = 1
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Grade ${subjectClass.gradeLevel} - ${subjectClass.section}",
                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
                // Optional: Schedule Time if available, else just schedule
                val timeRange = if (!subjectClass.startTime.isNullOrBlank() && !subjectClass.endTime.isNullOrBlank()) {
                    "${subjectClass.startTime} - ${subjectClass.endTime}"
                } else null
                
                val displaySchedule = listOfNotNull(subjectClass.classDays, timeRange).joinToString(" • ")

                if (displaySchedule.isNotBlank()) {
                     Text(
                        text = " • $displaySchedule",
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)),
                        maxLines = 1
                    )
                }
            }
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
    }
}

@Composable
fun ClassCard(subjectClass: SubjectClassEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = subjectClass.subjectName.take(1).uppercase(),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectClass.subjectName,
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colors.onSurface),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${subjectClass.gradeLevel} - ${subjectClass.section}",
                        style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    )
                }
            }
            
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
        }
    }
}
