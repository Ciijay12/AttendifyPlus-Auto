package com.attendifyplus.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.FilterList
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
import com.attendifyplus.ui.theme.SuccessGreen
import com.attendifyplus.ui.theme.WarningYellow
import com.attendifyplus.ui.theme.PillShape
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentHistoryScreen(
    navController: NavController,
    studentId: String,
    viewModel: StudentHistoryViewModel = getViewModel { parametersOf(studentId) },
    onLogout: () -> Unit = {}
) {
    val history by viewModel.history.collectAsState()
    val enrolledSubjects by viewModel.enrolledSubjects.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Stats calculation based on FILTERED history
    val present = history.count { it.status.equals("present", ignoreCase = true) }
    val late = history.count { it.status.equals("late", ignoreCase = true) }
    val absent = history.count { it.status.equals("absent", ignoreCase = true) }
    val total = history.size

    // Group history by Date for robust display
    val groupedHistory = remember(history) {
        history.groupBy { 
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
        }
    }

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Text(
                    text = "Attendance History",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
            }

            Spacer(Modifier.height(16.dp))
            
            // Subject Filter Chips
            if (enrolledSubjects.isNotEmpty()) {
                Text(
                    text = "Filter by Subject",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            text = "All Subjects",
                            selected = selectedSubject == null,
                            onClick = { viewModel.setSubjectFilter(null) }
                        )
                    }
                    lazyRowItems(enrolledSubjects) { subject ->
                        FilterChip(
                            text = subject.subjectName,
                            selected = selectedSubject == subject.subjectName,
                            onClick = { viewModel.setSubjectFilter(subject.subjectName) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Stats Card (Modified to show Counts instead of misleading Rate)
            Card(
                backgroundColor = PrimaryBlue,
                contentColor = Color.White,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCounter("Present", present)
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(30.dp).width(1.dp))
                    StatCounter("Late", late)
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(30.dp).width(1.dp))
                    StatCounter("Absent", absent)
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(30.dp).width(1.dp))
                    StatCounter("Total", total)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Grouped List by Date
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (selectedSubject != null) "No records for ${selectedSubject}." else "No attendance records found.", 
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    groupedHistory.forEach { (dateKey, items) ->
                        item {
                            // Date Header
                            Text(
                                text = prettyDate(dateKey),
                                style = MaterialTheme.typography.subtitle2.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        items.forEach { item ->
                            item {
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
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    if (item.status.equals("present", true)) Icons.Default.CheckCircle else Icons.Default.Schedule, 
                                                    contentDescription = null, 
                                                    tint = statusColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(Modifier.width(16.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.subject ?: "Homeroom",
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
                                Spacer(Modifier.height(8.dp))
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

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PrimaryBlue else Color.Transparent,
        contentColor = if (selected) Color.White else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
        shape = PillShape,
        border = if (!selected) BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)) else null,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.caption.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun StatCounter(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption.copy(color = Color.White.copy(alpha = 0.7f))
        )
    }
}

fun prettyDate(dateString: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    try {
        val date = sdf.parse(dateString) ?: return dateString
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val check = Calendar.getInstance().apply { time = date }
        
        // Reset time for check calendar to ensure day comparison works
        val itemDate = Calendar.getInstance().apply {
            set(check.get(Calendar.YEAR), check.get(Calendar.MONTH), check.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diff = today.timeInMillis - itemDate.timeInMillis
        val days = diff / (24 * 60 * 60 * 1000)

        return when (days) {
            0L -> "Today"
            1L -> "Yesterday"
            else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        return dateString
    }
}
