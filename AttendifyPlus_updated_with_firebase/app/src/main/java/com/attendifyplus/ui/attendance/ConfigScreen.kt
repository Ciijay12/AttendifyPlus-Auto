package com.attendifyplus.ui.attendance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel

@Composable
fun ConfigScreen(
    navController: NavController,
    studentViewModel: AdminStudentManagementViewModel = getViewModel(),
    subjectViewModel: AdminSubjectManagementViewModel = getViewModel(),
    calendarViewModel: SchoolCalendarViewModel = getViewModel()
) {
    val context = LocalContext.current
    
    // Instruction Dialog States
    var showStudentImportInstructions by remember { mutableStateOf(false) }
    var showSubjectImportInstructions by remember { mutableStateOf(false) }
    var showCalendarImportInstructions by remember { mutableStateOf(false) }

    // Status State
    val studentImportStatus by studentViewModel.importStatus.collectAsState()
    val subjectImportStatus by subjectViewModel.importStatus.collectAsState()
    val calendarImportStatus by calendarViewModel.importStatus.collectAsState()

    var showStatusDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    // Launchers
    val studentImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            studentViewModel.importStudentsFromCsv(context, it)
        }
    }

    val subjectImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            subjectViewModel.importSubjectsFromCsv(context, it)
        }
    }

    val calendarImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            calendarViewModel.importCalendarFromCsv(context, it)
        }
    }

    // Monitor Status
    LaunchedEffect(studentImportStatus) {
        studentImportStatus?.let {
            statusMessage = it
            showStatusDialog = true
            if (it.contains("Success", ignoreCase = true)) {
                delay(3000)
                studentViewModel.clearImportStatus()
                showStatusDialog = false
            }
        }
    }

    LaunchedEffect(subjectImportStatus) {
        subjectImportStatus?.let {
            statusMessage = it
            showStatusDialog = true
            if (it.contains("Success", ignoreCase = true)) {
                delay(3000)
                subjectViewModel.clearImportStatus()
                showStatusDialog = false
            }
        }
    }

    LaunchedEffect(calendarImportStatus) {
        calendarImportStatus?.let {
            statusMessage = it
            showStatusDialog = true
            if (it.contains("Success", ignoreCase = true)) {
                delay(3000)
                calendarViewModel.clearImportStatus()
                showStatusDialog = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Import & Export", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground) },
                backgroundColor = MaterialTheme.colors.background, // Match screen background to avoid rectangle look
                elevation = 0.dp,
                navigationIcon = {
                     IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onBackground)
                    }
                }
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp), // Move padding here to prevent shadow clipping
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Import Data",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                ConfigOptionCard(
                    title = "Import Students",
                    description = "Import student list from CSV",
                    icon = Icons.Default.PersonAdd,
                    onClick = { showStudentImportInstructions = true }
                )
            }
            
            item {
                ConfigOptionCard(
                    title = "Import Subjects",
                    description = "Import subjects list from CSV",
                    icon = Icons.Default.Class,
                    onClick = { showSubjectImportInstructions = true }
                )
            }

            item {
                ConfigOptionCard(
                    title = "Import Calendar Events",
                    description = "Import school holidays & events",
                    icon = Icons.Default.Event,
                    onClick = { showCalendarImportInstructions = true }
                )
            }

            item {
                Text(
                    text = "Export Data",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = SecondaryTeal),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                ConfigOptionCard(
                    title = "Export Attendance",
                    description = "Export attendance records to CSV",
                    icon = Icons.Default.FileDownload,
                    onClick = { navController.navigate("export_attendance") }
                )
            }

            item {
                ConfigOptionCard(
                    title = "Export Students",
                    description = "Export student lists to CSV",
                    icon = Icons.Default.PeopleAlt,
                    onClick = { navController.navigate("student_export") }
                )
            }
        }
    }

    // Student Import Instructions
    if (showStudentImportInstructions) {
        InstructionDialog(
            title = "Import Students Guidelines",
            content = """
                1. File Format: CSV (Comma Separated Values)
                2. Required Header Row:
                   First Name, Last Name, Grade, Section
                3. The system will skip the first row (header).
                4. Duplicate IDs are handled automatically.
                
                Recommended: Use a clean CSV file without special formatting.
            """.trimIndent(),
            onDismiss = { showStudentImportInstructions = false },
            onConfirm = {
                showStudentImportInstructions = false
                studentImportLauncher.launch(arrayOf("*/*"))
            }
        )
    }

    // Subject Import Instructions
    if (showSubjectImportInstructions) {
        InstructionDialog(
            title = "Import Subjects Guidelines",
            content = """
                1. File Format: CSV (Comma Separated Values)
                2. Required Header Row:
                   Subject Name, Grade Level
                3. Optional Columns (SHS):
                   Semester, Track, Type (Core/Applied/Specialized)
                4. The system will skip the first row (header).
                
                Ensure Grade Level matches the format (e.g., "7", "11").
            """.trimIndent(),
            onDismiss = { showSubjectImportInstructions = false },
            onConfirm = {
                showSubjectImportInstructions = false
                subjectImportLauncher.launch(arrayOf("*/*"))
            }
        )
    }
    
    // Calendar Import Instructions
    if (showCalendarImportInstructions) {
        InstructionDialog(
            title = "Import Calendar Guidelines",
            content = """
                1. File Format: CSV (Comma Separated Values)
                2. Required Header Row:
                   Date, Title, Description
                3. Date Format: yyyy-MM-dd (e.g., 2024-12-25)
                4. Optional Column: Type (holiday, exam, activity) - default is activity
                5. The system will skip the first row (header).
            """.trimIndent(),
            onDismiss = { showCalendarImportInstructions = false },
            onConfirm = {
                showCalendarImportInstructions = false
                calendarImportLauncher.launch(arrayOf("*/*"))
            }
        )
    }

    // Status Dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { 
                showStatusDialog = false 
                studentViewModel.clearImportStatus()
                subjectViewModel.clearImportStatus()
                calendarViewModel.clearImportStatus()
            },
            title = { Text(if (statusMessage.contains("Success")) "Success" else "Import Status") },
            text = { Text(statusMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showStatusDialog = false 
                    studentViewModel.clearImportStatus()
                    subjectViewModel.clearImportStatus()
                    calendarViewModel.clearImportStatus()
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ConfigOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp, // Increased elevation for better shadow visibility
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // Clip ripple to rounded corners
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = PrimaryBlue)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun InstructionDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = PrimaryBlue) },
        text = {
            Column {
                Text(content, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Select any file to proceed. Validation occurs after selection.", style = MaterialTheme.typography.caption, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)) {
                Text("Select File", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
