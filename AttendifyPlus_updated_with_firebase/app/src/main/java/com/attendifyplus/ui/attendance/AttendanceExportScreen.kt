package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.StandardScreen
import com.attendifyplus.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel

@Composable
fun AttendanceExportScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = getViewModel()
) {
    val context = LocalContext.current
    val subjects by viewModel.subjectClasses.collectAsState()
    val exportStatus by viewModel.message.collectAsState()
    val scaffoldState = rememberScaffoldState()

    // State
    var step by remember { mutableStateOf(1) }
    var selectedSubject by remember { mutableStateOf<SubjectClassEntity?>(null) }
    var selectedPeriod by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    var exportAll by remember { mutableStateOf(false) }
    var exportAllType by remember { mutableStateOf<String?>(null) } // "JHS" or "SHS"
    
    // Success/Error Visual State
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    // Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            // Pass specific "All_JHS" or "All_SHS" flag to VM
            val finalSubjectName = if (exportAll) "All_$exportAllType" else selectedSubject?.subjectName ?: "Unknown"
            viewModel.exportAttendance(it, context, selectedPeriod ?: "All", finalSubjectName)
        }
    }
    
    // Listen for export status updates
    LaunchedEffect(exportStatus) {
        exportStatus?.let { message ->
            statusMessage = message
            if (message.contains("success", ignoreCase = true)) {
                showSuccessDialog = true
                delay(3000) // Show for 3 seconds then navigate or close
                showSuccessDialog = false
                navController.popBackStack()
            } else if (message.contains("failed", ignoreCase = true) || message.contains("error", ignoreCase = true)) {
                showErrorDialog = true
            }
            // Clear message in VM? Might loop if we don't. Assuming VM clears it or emits once.
            // A better approach is handling events, but here we just show dialog.
        }
    }

    // Intercept Back Press
    BackHandler(enabled = step > 1) {
        if (step > 1) {
            step--
        }
    }
    
    if (showSuccessDialog) {
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        tint = SuccessGreen, 
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Export Successful!", 
                        style = MaterialTheme.typography.h6, 
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(statusMessage, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                }
            }
        }
    }
    
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Export Failed", color = MaterialTheme.colors.error) },
            text = { Text(statusMessage, color = MaterialTheme.colors.onSurface) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Export Attendance", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (step > 1) step-- else navController.popBackStack() 
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colors.onSurface)
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                contentColor = MaterialTheme.colors.onSurface
            )
        }
    ) { padding ->
        // StandardScreen is assumed to handle background color correctly from theme
        StandardScreen(modifier = Modifier.padding(padding)) {
            // Progress Indicator with Label
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     Text("Progress", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                     Text("Step $step of 3", style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                 }
                 Spacer(Modifier.height(8.dp))
                 LinearProgressIndicator(
                    progress = step / 3f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PrimaryBlue,
                    backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                )
            }

            when (step) {
                1 -> SubjectSelectionStep(
                    subjects = subjects,
                    onSubjectSelected = { subject ->
                        selectedSubject = subject
                        exportAll = false
                        step = 2
                    },
                    onExportAllJhs = {
                        exportAll = true
                        selectedSubject = null
                        exportAllType = "JHS"
                        step = 2
                    },
                    onExportAllShs = {
                        exportAll = true
                        selectedSubject = null
                        exportAllType = "SHS"
                        step = 2
                    }
                )
                2 -> PeriodSelectionStep(
                    isSeniorHigh = if (exportAll) (exportAllType == "SHS") else (selectedSubject?.gradeLevel?.toIntOrNull()?.let { it >= 11 } ?: false),
                    onPeriodSelected = { period ->
                        selectedPeriod = period
                        step = 3
                    }
                )
                3 -> {
                    // Confirmation Logic
                    LaunchedEffect(Unit) { showConfirmation = true }
                    if (showConfirmation) {
                        ConfirmationDialog(
                            subject = if (exportAll) "All Subjects ($exportAllType)" else selectedSubject?.subjectName ?: "",
                            period = selectedPeriod ?: "",
                            onConfirm = {
                                showConfirmation = false
                                val fileName = "Attendance_${if(exportAll) "All_$exportAllType" else selectedSubject?.subjectName}_${selectedPeriod}_${System.currentTimeMillis()}.csv"
                                exportLauncher.launch(fileName)
                            },
                            onDismiss = { showConfirmation = false; step = 2 }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectSelectionStep(
    subjects: List<SubjectClassEntity>,
    onSubjectSelected: (SubjectClassEntity) -> Unit,
    onExportAllJhs: () -> Unit,
    onExportAllShs: () -> Unit
) {
    Column {
        Text(
            "Step 1: Choose Subject",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground
        )
        Text(
            "Select the class or group you want to export.",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(24.dp))

        // Option to Export All - JHS (Stylish Gradient Card)
        GradientActionCard(
            title = "Export All JHS Records",
            subtitle = "Quarterly format (Grade 7-10)",
            icon = Icons.Default.School,
            gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2)), // Blue Gradient
            onClick = onExportAllJhs
        )

        Spacer(Modifier.height(12.dp))

        // Option to Export All - SHS (Stylish Gradient Card)
        GradientActionCard(
            title = "Export All SHS Records",
            subtitle = "Semester format (Grade 11-12)",
            icon = Icons.Default.School, // Same icon but different color theme
            gradientColors = listOf(Color(0xFF26A69A), Color(0xFF00695C)), // Teal Gradient
            onClick = onExportAllShs
        )

        Spacer(Modifier.height(32.dp))
        Text("Specific Classes", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(subjects) { subject ->
                ClassSelectionCard(subject = subject, onClick = { onSubjectSelected(subject) })
            }
        }
    }
}

@Composable
fun GradientActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White)
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ClassSelectionCard(
    subject: SubjectClassEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Letter Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.subjectName.firstOrNull()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(subject.subjectName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colors.onSurface)
                Text("${subject.gradeLevel} - ${subject.section}", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowForwardIos, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
        }
    }
}


@Composable
fun PeriodSelectionStep(
    isSeniorHigh: Boolean,
    onPeriodSelected: (String) -> Unit
) {
    Column {
        Text(
            "Step 2: Select Period",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
             color = MaterialTheme.colors.onBackground
        )
         Text(
            if (isSeniorHigh) "Grading periods for Senior High School." else "Grading periods for Junior High School.",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(24.dp))

        if (isSeniorHigh) {
            Text("1st Semester", fontWeight = FontWeight.Bold, color = SecondaryTeal, modifier = Modifier.padding(vertical = 8.dp))
            StylishPeriodOption("1st Quarter", "Q1", onPeriodSelected)
            StylishPeriodOption("2nd Quarter", "Q2", onPeriodSelected)
            StylishPeriodOption("Whole Semester", "Sem1", onPeriodSelected, isHighlighted = true)
            
            Spacer(Modifier.height(24.dp))
            
            Text("2nd Semester", fontWeight = FontWeight.Bold, color = SecondaryTeal, modifier = Modifier.padding(vertical = 8.dp))
            StylishPeriodOption("3rd Quarter", "Q3", onPeriodSelected)
            StylishPeriodOption("4th Quarter", "Q4", onPeriodSelected)
            StylishPeriodOption("Whole Semester", "Sem2", onPeriodSelected, isHighlighted = true)
        } else {
            Text("Academic Year", fontWeight = FontWeight.Bold, color = PrimaryBlue, modifier = Modifier.padding(vertical = 8.dp))
            StylishPeriodOption("1st Quarter", "Q1", onPeriodSelected)
            StylishPeriodOption("2nd Quarter", "Q2", onPeriodSelected)
            StylishPeriodOption("3rd Quarter", "Q3", onPeriodSelected)
            StylishPeriodOption("4th Quarter", "Q4", onPeriodSelected)
            Spacer(Modifier.height(16.dp))
            StylishPeriodOption("Whole Year", "All", onPeriodSelected, isHighlighted = true)
        }
    }
}

@Composable
fun StylishPeriodOption(label: String, value: String, onClick: (String) -> Unit, isHighlighted: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick(value) },
        shape = RoundedCornerShape(12.dp),
        elevation = if (isHighlighted) 4.dp else 0.dp,
        backgroundColor = if (isHighlighted) PrimaryBlue else MaterialTheme.colors.surface, // Blue or Surface
        border = if (!isHighlighted) BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlighted) Color.White else MaterialTheme.colors.onSurface
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isHighlighted) Color.White.copy(alpha = 0.7f) else MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    subject: String,
    period: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Export", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
        text = {
            Column {
                Text("You are about to export attendance records for:", color = MaterialTheme.colors.onSurface)
                Spacer(Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Class, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(subject, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                }
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = SecondaryTeal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(period, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export CSV", color = Color.White, modifier = Modifier.padding(4.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            }
        },
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.surface
    )
}
