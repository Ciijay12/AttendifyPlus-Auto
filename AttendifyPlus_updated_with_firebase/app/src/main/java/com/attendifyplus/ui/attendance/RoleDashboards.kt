package com.attendifyplus.ui.attendance

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.ui.theme.DeepPurple
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.RoyalIndigo
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.SuccessGreen
import com.attendifyplus.util.QRGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.getViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.attendifyplus.ui.components.InformationBoardCard
import com.attendifyplus.ui.theme.LocalWindowInfo
import com.attendifyplus.ui.theme.WindowInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TeacherDashboard(
    navController: NavController,
    userName: String = "", // Added userName parameter
    viewModel: TeacherDashboardViewModel = getViewModel()
) {
    val dailyStatus by viewModel.dailyStatus.collectAsState()
    val subjectClasses by viewModel.subjectClasses.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    
    val windowInfo = LocalWindowInfo.current
    val isWideScreen = windowInfo.screenWidthInfo is WindowInfo.WindowType.Medium ||
            windowInfo.screenWidthInfo is WindowInfo.WindowType.Expanded
    
    // Collect values from ViewModel instead of using hardcoded defaults
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val currentPeriod by viewModel.schoolPeriod.collectAsState()

    var showGenerateQrDialog by remember { mutableStateOf(false) }
    var showMyQrDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd")) }

    LaunchedEffect(selectedStudent, showMyQrDialog) {
        if (showMyQrDialog && selectedStudent != null) {
            while (isActive) {
                val timestamp = System.currentTimeMillis()
                val data = "${selectedStudent!!.id}|$timestamp"
                qrCodeBitmap = QRGenerator.generate(data)
                delay(15000) // Regenerate every 15 seconds
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        // Wavy Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, size.height * 0.75f)
                    cubicTo(
                        size.width * 0.25f, size.height,
                        size.width * 0.75f, size.height * 0.5f,
                        size.width, size.height * 0.85f
                    )
                    lineTo(size.width, 0f)
                    close()
                }
                
                // Draw Shadow
                translate(left = 0f, top = 15f) {
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.2f)
                    )
                }

                // Draw Gradient
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(RoyalIndigo, DeepPurple)
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Added status bar padding
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp)) // Adjusted top spacing
            
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Good Day, ${if (userName.isNotBlank()) userName else "Teacher"}!",
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SyncButton(syncState = syncState, onSync = { viewModel.refresh(force = true) })
                    
                    Spacer(Modifier.width(8.dp))

                    // Profile / Settings Icon
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = { /* Navigate to profile */ }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))

            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Information Board (Floating over header)
                        // Updated to navigate to Read-Only Calendar
                        InformationBoardCard(
                            upcomingEvents = upcomingEvents,
                            currentPeriod = currentPeriod,
                            dailyStatus = dailyStatus,
                            onViewCalendar = { navController.navigate("read_only_school_calendar") },
                            onEditStatus = { showStatusDialog = true }
                        )
                        Spacer(Modifier.height(100.dp)) // Bottom padding for scroll
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                         // Classes Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Classes",
                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                            )
                            
                            if (subjectClasses.isNotEmpty()) {
                                TextButton(onClick = { navController.navigate("manage_classes") }) {
                                    Text("View All", color = PrimaryBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (subjectClasses.isEmpty()) {
                            EmptyStateCard()
                        } else {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = 2.dp,
                                backgroundColor = MaterialTheme.colors.surface,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentPadding = PaddingValues(bottom = 100.dp)
                                ) {
                                    items(subjectClasses) { subjectClass ->
                                        CompactClassItem(
                                            subjectClass = subjectClass, 
                                            onClick = {
                                                val encSubject = java.net.URLEncoder.encode(subjectClass.subjectName, "UTF-8")
                                                val encGrade = java.net.URLEncoder.encode(subjectClass.gradeLevel, "UTF-8")
                                                val encSection = java.net.URLEncoder.encode(subjectClass.section, "UTF-8")
                                                navController.navigate("class_dashboard/$encSubject/$encGrade/$encSection")
                                            }
                                        )
                                        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Portrait / Compact Mode
                // Information Board (Floating over header)
                // Updated to navigate to Read-Only Calendar
                InformationBoardCard(
                    upcomingEvents = upcomingEvents,
                    currentPeriod = currentPeriod,
                    dailyStatus = dailyStatus,
                    onViewCalendar = { navController.navigate("read_only_school_calendar") },
                    onEditStatus = { showStatusDialog = true }
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Classes Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Classes",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                    )
                    
                    if (subjectClasses.isNotEmpty()) {
                        TextButton(onClick = { navController.navigate("manage_classes") }) {
                            Text("View All", color = PrimaryBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                if (subjectClasses.isEmpty()) {
                    EmptyStateCard()
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = 2.dp,
                        backgroundColor = MaterialTheme.colors.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(subjectClasses) { subjectClass ->
                                CompactClassItem(
                                    subjectClass = subjectClass, 
                                    onClick = {
                                        val encSubject = java.net.URLEncoder.encode(subjectClass.subjectName, "UTF-8")
                                        val encGrade = java.net.URLEncoder.encode(subjectClass.gradeLevel, "UTF-8")
                                        val encSection = java.net.URLEncoder.encode(subjectClass.section, "UTF-8")
                                        navController.navigate("class_dashboard/$encSubject/$encGrade/$encSection")
                                    }
                                )
                                Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }

        if (showGenerateQrDialog) {
            GenerateQrCodeDialog(
                onDismiss = { showGenerateQrDialog = false },
                viewModel = viewModel,
                onGenerate = { student ->
                    selectedStudent = student
                    showGenerateQrDialog = false
                    showMyQrDialog = true
                }
            )
        }

        if (showMyQrDialog) {
            selectedStudent?.let {
                MyQrDialog(
                    onDismiss = { showMyQrDialog = false },
                    studentName = "${it.firstName} ${it.lastName}",
                    studentId = it.id,
                    qrCodeBitmap = qrCodeBitmap,
                    studentGradeSection = "${it.grade} - ${it.section}"
                )
            }
        }
        
        if (showStatusDialog) {
            EditStatusDialog(
                onDismiss = { showStatusDialog = false },
                onSave = { isNoClass, reason ->
                    viewModel.updateDailyStatus(isNoClass, reason)
                    showStatusDialog = false
                }
            )
        }
    }
}

@Composable
fun EditStatusDialog(
    onDismiss: () -> Unit,
    onSave: (Boolean, String) -> Unit
) {
    var isNoClass by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Daily Class Status",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Set the attendance status for today.",
                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "Class Day",
                        selected = !isNoClass,
                        color = SuccessGreen,
                        icon = Icons.Default.CheckCircle,
                        onClick = { isNoClass = false }
                    )
                    
                    StatusOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "No Class",
                        selected = isNoClass,
                        color = Color.Red,
                        icon = Icons.Default.Warning,
                        onClick = { isNoClass = true }
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                if (isNoClass) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { 
                            reason = it
                            error = false
                        },
                        label = { Text("Reason (Required)") },
                        isError = error,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            textColor = MaterialTheme.colors.onSurface
                        )
                    )
                    if (error) {
                        Text(
                            text = "Reason is required to save status.", 
                            color = Color.Red, 
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                } else {
                    // Spacer to maintain some height if needed or just standard spacing
                    Spacer(Modifier.height(8.dp))
                }

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
                            if (isNoClass && reason.isBlank()) {
                                error = true
                            } else {
                                onSave(isNoClass, reason)
                            }
                        },
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Update Status", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GenerateQrCodeDialog(
    onDismiss: () -> Unit,
    viewModel: TeacherDashboardViewModel,
    onGenerate: (StudentEntity) -> Unit
) {
    val students by viewModel.allStudents.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate QR Code", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
        text = {
            Column {
                Text(
                    "Select a student to generate a secure, time-limited QR code for attendance.",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    OutlinedTextField(
                        value = selectedStudent?.let { "${it.firstName} ${it.lastName}" } ?: "Select Student",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { expanded = !expanded },
                                tint = MaterialTheme.colors.onSurface
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onSurface,
                            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        students.forEach { student ->
                            DropdownMenuItem(onClick = {
                                selectedStudent = student
                                expanded = false
                            }) {
                                Text("${student.firstName} ${student.lastName}")
                            }
                        }
                    }
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        selectedStudent?.let { onGenerate(it) }
                    },
                    enabled = selectedStudent != null,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                ) {
                    Text("Generate", color = Color.White)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.surface
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdminDashboard(
    navController: NavController,
    onTeachers: () -> Unit,
    // onConfig removed or handled internally if not needed
    viewModel: AdminDashboardViewModel = getViewModel()
) {
    val dailyStatus by viewModel.dailyStatus.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val currentPeriod by viewModel.schoolPeriod.collectAsState()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    
    val windowInfo = LocalWindowInfo.current
    val isWideScreen = windowInfo.screenWidthInfo is WindowInfo.WindowType.Medium ||
            windowInfo.screenWidthInfo is WindowInfo.WindowType.Expanded
    
    // State for Edit Dialog (Added for Admin too as requested "for the admin and the teacher")
    var showStatusDialog by remember { mutableStateOf(false) }
    
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd")) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        // Wavy Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, size.height * 0.75f)
                    cubicTo(
                        size.width * 0.25f, size.height,
                        size.width * 0.75f, size.height * 0.5f,
                        size.width, size.height * 0.85f
                    )
                    lineTo(size.width, 0f)
                    close()
                }
                
                // Draw Shadow
                translate(left = 0f, top = 15f) {
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.2f)
                    )
                }

                // Draw Gradient
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(RoyalIndigo, DeepPurple)
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Day, Admin!",
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
                
                // Profile / Settings Icon
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = { /* Navigate to profile */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))

            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    ) {
                        InformationBoardCard(
                            upcomingEvents = upcomingEvents,
                            currentPeriod = currentPeriod,
                            dailyStatus = dailyStatus,
                            onViewCalendar = { navController.navigate("school_calendar") }, // Admin still goes to full calendar
                            onEditStatus = { showStatusDialog = true }
                        )
                        Spacer(Modifier.height(100.dp))
                    }
                    
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        // Quick Actions Section
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colors.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Adapted for narrow column in wide screen -> maybe Column of cards instead of Row
                        // But weight 1f is half screen, enough for Row of 3 buttons? Maybe.
                        // Let's use FlowRow if possible, or just standard Row if it fits. 
                        // Tablets are wide. Half tablet is wide enough.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionCard(
                                title = "Teachers",
                                icon = Icons.Default.Person,
                                color = SecondaryTeal,
                                modifier = Modifier.weight(1f),
                                onClick = onTeachers
                            )
                            
                            QuickActionCard(
                                title = "Students",
                                icon = Icons.Default.School,
                                color = PrimaryBlue,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("admin_student_management") }
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                             QuickActionCard(
                                title = "Events",
                                icon = Icons.Default.Event,
                                color = DeepPurple,
                                modifier = Modifier.weight(0.5f), // Take half space or full?
                                onClick = { navController.navigate("school_calendar") }
                            )
                            Spacer(Modifier.weight(0.5f)) // Balance
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Sync Status Card
                        SyncStatusCard(
                            unsyncedCount = unsyncedCount, 
                            lastSyncTime = lastSyncTimestamp,
                            syncState = syncState,
                            onSyncNow = { viewModel.refresh() } 
                        )
                        
                        Spacer(Modifier.height(100.dp))
                    }
                }
            } else {
                // Portrait / Compact
                InformationBoardCard(
                    upcomingEvents = upcomingEvents,
                    currentPeriod = currentPeriod,
                    dailyStatus = dailyStatus,
                    onViewCalendar = { navController.navigate("school_calendar") }, // Admin still goes to full calendar
                    onEditStatus = { showStatusDialog = true }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick Actions Section
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Teachers",
                        icon = Icons.Default.Person,
                        color = SecondaryTeal,
                        modifier = Modifier.weight(1f),
                        onClick = onTeachers
                    )
                    
                    QuickActionCard(
                        title = "Students",
                        icon = Icons.Default.School,
                        color = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("admin_student_management") } // Corrected route
                    )

                    QuickActionCard(
                        title = "Events",
                        icon = Icons.Default.Event,
                        color = DeepPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("school_calendar") } // Corrected route
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sync Status Card
                SyncStatusCard(
                    unsyncedCount = unsyncedCount, 
                    lastSyncTime = lastSyncTimestamp,
                    syncState = syncState,
                    onSyncNow = { viewModel.refresh() } // Trigger sync via refresh function
                )
            }
        }
        
        if (showStatusDialog) {
             EditStatusDialog(
                onDismiss = { showStatusDialog = false },
                onSave = { isNoClass, reason ->
                    viewModel.updateDailyStatus(isNoClass, reason)
                    showStatusDialog = false
                }
            )
        }
    }
}
