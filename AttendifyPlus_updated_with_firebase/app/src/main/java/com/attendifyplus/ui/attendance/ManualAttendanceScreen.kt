package com.attendifyplus.ui.attendance

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.StandardScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.io.File
import java.net.URLEncoder

@Composable
fun ManualAttendanceScreen(
    navController: NavController? = null,
    studentListViewModel: StudentListViewModel = getViewModel(),
    attendanceViewModel: AttendanceViewModel = getViewModel(),
    onLogout: () -> Unit = {}
) {
    // Context & Scopes
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Use rememberScaffoldState to control snackbar
    val scaffoldState = rememberScaffoldState()
    val message by attendanceViewModel.message.collectAsState()
    
    // Dialog State
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Absentee Marking States
    var showMarkAbsenteesDialog by remember { mutableStateOf(false) }
    val subjectClasses by attendanceViewModel.subjectClasses.collectAsState()

    // Message Handling
    LaunchedEffect(message) {
        message?.let {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }

    if (showMarkAbsenteesDialog) {
        MarkAbsenteesDialog(
            onDismiss = { showMarkAbsenteesDialog = false },
            classes = subjectClasses,
            onMark = { clazz ->
                attendanceViewModel.markAbsentees(clazz.gradeLevel, clazz.section, "subject", clazz.subjectName)
                showMarkAbsenteesDialog = false
            }
        )
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
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        StandardScreen(modifier = Modifier.padding(paddingValues)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "Tools",
                        style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                    )
                    Text(
                        text = "Manage Attendance Records",
                        style = MaterialTheme.typography.subtitle1.copy(color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // New Feature: Mark Remaining as Absent
            ToolCard(
                title = "Mark Absentees",
                description = "Automatically mark all remaining students in a class as absent for today.",
                icon = Icons.Default.EventBusy,
                color = Color.Red,
                onClick = { showMarkAbsenteesDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Feature 1: Retroactive Attendance (Record Skipped Attendance)
            ToolCard(
                title = "Record Skipped Attendance",
                description = "Log attendance for past dates manually.",
                icon = Icons.Default.History,
                color = SecondaryTeal,
                onClick = { navController?.navigate("retroactive_attendance") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Feature 3: Export Attendance
            ToolCard(
                title = "Export Attendance",
                description = "Download class attendance reports.",
                icon = Icons.Default.Download,
                color = Color(0xFFFFA726), // Orange
                onClick = { 
                     navController?.navigate("export_attendance")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MarkAbsenteesDialog(
    onDismiss: () -> Unit,
    classes: List<SubjectClassEntity>,
    onMark: (SubjectClassEntity) -> Unit
) {
    var selectedClass by remember { mutableStateOf<SubjectClassEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark Remaining as Absent", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
        text = {
            Column {
                Text("Select a class to mark all students who haven't clocked in today as ABSENT.", color = MaterialTheme.colors.onSurface)
                Spacer(Modifier.height(16.dp))
                
                if (classes.isEmpty()) {
                    Text("No classes found.", color = Color.Gray)
                } else {
                    classes.forEach { clazz ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedClass = clazz }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedClass == clazz,
                                onClick = { selectedClass = clazz },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue, unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(clazz.subjectName, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                                Text("${clazz.gradeLevel} - ${clazz.section}", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedClass?.let { onMark(it) } },
                enabled = selectedClass != null,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Mark Absent", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            }
        },
        backgroundColor = MaterialTheme.colors.surface
    )
}

@Composable
fun ToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
