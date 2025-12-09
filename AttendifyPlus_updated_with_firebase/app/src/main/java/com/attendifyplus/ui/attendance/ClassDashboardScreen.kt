package com.attendifyplus.ui.attendance

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.ui.components.SummaryCard
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.SuccessGreen
import com.attendifyplus.ui.theme.WarningYellow
import com.attendifyplus.util.PrintUtils
import com.attendifyplus.util.QRGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.androidx.compose.getViewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClassDashboardScreen(
    navController: NavController,
    subjectName: String,
    grade: String,
    section: String,
    viewModel: ClassDashboardViewModel = getViewModel()
) {
    val present by viewModel.presentCount.collectAsState()
    val late by viewModel.lateCount.collectAsState()
    val absent by viewModel.absentCount.collectAsState()
    val students by viewModel.students.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    
    var showHistory by remember { mutableStateOf(false) }
    // Help Dialog State
    var showHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    
    // QR Dialog State
    var showQrDialog by remember { mutableStateOf(false) }
    var studentForQr by remember { mutableStateOf<Pair<String, String>?>(null) } // Name, ID

    // Edit Status Dialog State
    var showUpdateStatusDialog by remember { mutableStateOf(false) }
    var studentToUpdate by remember { mutableStateOf<StudentWithStatus?>(null) }
    
    // Fetch Alert Dialog State (New Feature)
    var showFetchDialog by remember { mutableStateOf(false) }

    // Handle Back Press
    BackHandler {
        navController.popBackStack()
    }

    LaunchedEffect(subjectName) {
        viewModel.loadStatsForSubject(subjectName)
        viewModel.loadStudents(grade, section, subjectName)
        
        // Check for empty list after a brief delay to allow DB load
        delay(500)
        if (viewModel.students.value.isEmpty()) {
            showFetchDialog = true
        }
    }
    
    // Show import status in Snackbar
    LaunchedEffect(importStatus) {
        importStatus?.let { message ->
            scaffoldState.snackbarHostState.showSnackbar(message)
            viewModel.clearImportStatus()
        }
    }

    if (showHistory) {
        ClassHistoryDialog(
            historyFlow = viewModel.history,
            onDismiss = { showHistory = false },
            onDelete = { viewModel.deleteAttendance(it) }
        )
    }
    
    // Fetch Data Alert (New)
    if (showFetchDialog) {
        AlertDialog(
            onDismissRequest = { showFetchDialog = false },
            title = { Text("Fetch Class Roster?", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
            text = { 
                Text("No students found for this class yet.\n\nClick 'Fetch' to load students from the admin master list for Grade $grade - $section.", 
                    color = MaterialTheme.colors.onSurface
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.fetchStudents(context) // Use fetchStudents now
                        showFetchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                ) {
                    Text("Fetch Data", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFetchDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        )
    }
    
    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Default Student Credentials", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
            text = {
                Column {
                    Text("Inform your students to use the following credentials for their first login:", color = MaterialTheme.colors.onSurface)
                    Spacer(Modifier.height(16.dp))
                    Text("Username:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    Text("Their Student ID (e.g., S-2024-1234)", style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text("Password:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    Text("123456", style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onSurface)
                    Spacer(Modifier.height(16.dp))
                    Text("They will be able to change this after logging in.", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        )
    }
    
    // QR Dialog
    if (showQrDialog && studentForQr != null) {
        val (name, id) = studentForQr!!
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                    )
                    Text(
                        text = "ID: $id",
                        style = MaterialTheme.typography.subtitle1.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    val payload = JSONObject().put("t", "student").put("i", id).toString()
                    val bitmap = QRGenerator.generate(payload)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Text("Error generating QR", color = MaterialTheme.colors.onSurface)
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            if (bitmap != null) {
                                PrintUtils.printBitmap(context, bitmap, "QR_$name")
                            }
                        },
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = SecondaryTeal)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Print QR", color = Color.White)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showQrDialog = false },
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }

    // Update Status Dialog
    if (showUpdateStatusDialog && studentToUpdate != null) {
        UpdateStatusDialog(
            studentName = "${studentToUpdate!!.student.firstName} ${studentToUpdate!!.student.lastName}",
            currentStatus = studentToUpdate!!.status,
            onDismiss = { showUpdateStatusDialog = false },
            onSave = { status ->
                viewModel.updateAttendanceStatus(studentToUpdate!!.student.id, status, subjectName)
                showUpdateStatusDialog = false
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.fetchStudents(context) },
                backgroundColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Sync, contentDescription = "Fetch Students")
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding() // Added fix for status bar overlap
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colors.surface,
                        elevation = 2.dp
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subjectName,
                            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue),
                            maxLines = 1
                        )
                        Text(
                            text = "$grade - $section",
                            style = MaterialTheme.typography.subtitle1.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Info Icon (Help)
                        IconButton(
                            onClick = { showHelpDialog = true },
                            modifier = Modifier
                                .background(MaterialTheme.colors.surface, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Help Info", tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                        }

                        Spacer(Modifier.width(8.dp))

                        // History Icon
                        IconButton(
                            onClick = { showHistory = true },
                            modifier = Modifier
                                .background(MaterialTheme.colors.surface, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = "History Log", tint = PrimaryBlue)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Daily Summary Card
                SummaryCard(
                    present = present,
                    late = late,
                    absent = absent
                )
                
                Spacer(Modifier.height(24.dp))

                // Students List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Students (${students.size})",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colors.onSurface),
                    )
                    
                    IconButton(onClick = { viewModel.toggleSort() }) {
                        Icon(Icons.Default.SortByAlpha, contentDescription = "Sort", tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No students enrolled in this class yet.", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    showFetchDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = SecondaryTeal.copy(alpha = 0.1f)),
                                elevation = ButtonDefaults.elevation(0.dp)
                            ) {
                                Text("Fetch from Directory", color = SecondaryTeal)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(students) { item ->
                            val student = item.student
                            val username = student.username ?: student.id
                            val password = student.password ?: "123456"
                            StudentCardWithActions(
                                name = "${student.lastName}, ${student.firstName}",
                                status = item.status,
                                username = username,
                                password = password,
                                onClick = {
                                    studentToUpdate = item
                                    showUpdateStatusDialog = true
                                },
                                onQr = {
                                    studentForQr = "${student.firstName} ${student.lastName}" to student.id
                                    showQrDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCardWithActions(
    name: String, 
    status: String,
    username: String,
    password: String,
    onClick: () -> Unit,
    onQr: () -> Unit
) {
    val statusColor = when(status.lowercase()) {
        "present" -> SuccessGreen
        "late" -> WarningYellow
        "absent" -> Color.Red
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    }
    
    val statusText = if (status == "none") "Unmarked" else status.replaceFirstChar { it.uppercase() }
    val statusContentColor = if (status == "none") MaterialTheme.colors.onSurface.copy(alpha = 0.6f) else statusColor

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // New Compact Row Layout
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Avatar
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // 2. Name, Status, and Credentials (Column)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colors.onSurface),
                    maxLines = 1
                )
                
                // Credentials
                Text(
                    text = "User: $username | Pass: $password",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontSize = 10.sp)
                )
                
                // Status pill right under name, smaller
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        color = statusContentColor,
                        style = MaterialTheme.typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            // 3. Action Icons (Row)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onQr, modifier = Modifier.size(35.dp)) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR", tint = PrimaryBlue, modifier = Modifier.size(25.dp))
                }
            }
        }
    }
}

@Composable
fun UpdateStatusDialog(
    studentName: String,
    currentStatus: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var status by remember { mutableStateOf(currentStatus.lowercase()) } // "present", "late", "absent", "none"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Update Status",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colors.onSurface)
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Status Selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { status = "present" }.padding(vertical = 4.dp)) {
                        RadioButton(selected = status == "present", onClick = { status = "present" }, colors = RadioButtonDefaults.colors(selectedColor = SuccessGreen, unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)))
                        Spacer(Modifier.width(8.dp))
                        Text("Present", color = if(status == "present") SuccessGreen else MaterialTheme.colors.onSurface)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { status = "late" }.padding(vertical = 4.dp)) {
                        RadioButton(selected = status == "late", onClick = { status = "late" }, colors = RadioButtonDefaults.colors(selectedColor = WarningYellow, unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)))
                        Spacer(Modifier.width(8.dp))
                        Text("Late", color = if(status == "late") WarningYellow else MaterialTheme.colors.onSurface)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { status = "absent" }.padding(vertical = 4.dp)) {
                        RadioButton(selected = status == "absent", onClick = { status = "absent" }, colors = RadioButtonDefaults.colors(selectedColor = Color.Red, unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)))
                        Spacer(Modifier.width(8.dp))
                        Text("Absent", color = if(status == "absent") Color.Red else MaterialTheme.colors.onSurface)
                    }
                     Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { status = "none" }.padding(vertical = 4.dp)) {
                        RadioButton(selected = status == "none" || status == "unmarked", onClick = { status = "none" }, colors = RadioButtonDefaults.colors(selectedColor = Color.Gray, unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)))
                        Spacer(Modifier.width(8.dp))
                        Text("Unmarked", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(status)
                        },
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ClassHistoryDialog(
    historyFlow: StateFlow<List<AttendanceEntity>>,
    onDismiss: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val history by historyFlow.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colors.onSurface)
                    }
                }
                
                Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                
                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No records found", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { item ->
                            val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Updated at: $dateStr", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                                    Text(text = "Status: ${item.status}", style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)))
                                }
                                IconButton(onClick = { onDelete(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}
