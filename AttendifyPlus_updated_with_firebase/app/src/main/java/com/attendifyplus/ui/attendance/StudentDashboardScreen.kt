package com.attendifyplus.ui.attendance

import android.app.Activity
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.ui.components.InformationBoardCard
import com.attendifyplus.ui.theme.DeepPurple
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.RoyalIndigo
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.SuccessGreen
import com.attendifyplus.ui.theme.WarningYellow
import com.attendifyplus.ui.theme.LocalWindowInfo
import com.attendifyplus.ui.theme.WindowInfo
import com.attendifyplus.util.QRGenerator
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StudentDashboardScreen(
    navController: NavController,
    role: String,
    studentId: String,
    onLogout: () -> Unit = {},
    viewModel: StudentDashboardViewModel = getViewModel()
) {
    var currentTab by remember { mutableStateOf(0) }
    var showMyQrDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val dailyStatus by viewModel.dailyStatus.collectAsState()
    val subjectClassesWithStatus by viewModel.subjectClassesWithStatus.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val advisoryInfo by viewModel.advisoryInfo.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val currentPeriod by viewModel.schoolPeriod.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd")) }
    
    val windowInfo = LocalWindowInfo.current

    // System Bar Icon Control
    val useDarkIcons = currentTab != 0
    SetSystemBarIcons(useDarkIcons = useDarkIcons)

    // Load Student Details on Launch
    LaunchedEffect(studentId) {
        viewModel.loadStudentDetails(studentId)
    }

    LaunchedEffect(showMyQrDialog) {
        if (showMyQrDialog) {
            while (true) {
                val timestamp = System.currentTimeMillis()
                val data = "$studentId|$timestamp"
                qrCodeBitmap = QRGenerator.generate(data)
                delay(15000) // Regenerate every 15 seconds
            }
        }
    }

    // Intercept Back Press
    BackHandler(enabled = true) {
        if (currentTab != 0) {
            currentTab = 0
        } else {
            showExitDialog = true
        }
    }

    if (showMyQrDialog) {
        val gradeSection = if (advisoryInfo.grade.isNotBlank()) "Grade ${advisoryInfo.grade} - ${advisoryInfo.section}" else ""
        MyQrDialog(
            onDismiss = { showMyQrDialog = false },
            studentName = userName,
            studentId = studentId,
            qrCodeBitmap = qrCodeBitmap,
            studentGradeSection = gradeSection
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = "Exit App", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
            text = { Text("Are you sure you want to exit the application?", color = MaterialTheme.colors.onSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Exit", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    // Root Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .navigationBarsPadding() // Fix: Ensure bottom nav is not covered by system nav
    ) {
        // Wavy Header (Only for Home Tab)
        if (currentTab == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
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
        }

        // Content Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 88.dp) // Student always has bottom bar
        ) {
            when (currentTab) {
                0 -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                         // Header Section for Student
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Good Day, $userName!",
                                    style = MaterialTheme.typography.h5.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                )
                                if (advisoryInfo.grade.isNotBlank()) {
                                    Text(
                                        text = "Grade ${advisoryInfo.grade} - ${advisoryInfo.section}",
                                        style = MaterialTheme.typography.subtitle2.copy(
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    )
                                }
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
                        
                        Spacer(modifier = Modifier.height(60.dp)) // Push content below wavy header
                        
                        // Main Content (Scrollable)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Information Board
                            InformationBoardCard(
                                upcomingEvents = upcomingEvents,
                                currentPeriod = currentPeriod,
                                dailyStatus = dailyStatus,
                                onViewCalendar = {}
                            )

                            // 2. Enrolled Advisory Class (Only if assigned)
                            if (advisoryInfo.grade.isNotBlank()) {
                                AdvisoryClassCard(
                                    grade = advisoryInfo.grade,
                                    section = advisoryInfo.section,
                                    track = advisoryInfo.track,
                                    adviserName = advisoryInfo.adviserName
                                )
                            }

                            // 3. My Subjects (Renamed)
                            Column {
                                Text(
                                    text = "My Subjects",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (subjectClassesWithStatus.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No subjects enrolled yet.",
                                            style = MaterialTheme.typography.body1,
                                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                } else {
                                    // List of subjects
                                    subjectClassesWithStatus.forEach { subjectWithStatus ->
                                        ClassCard(
                                            subjectClass = subjectWithStatus.subject, 
                                            status = subjectWithStatus.status,
                                            teacherName = subjectWithStatus.teacherName,
                                            onClick = { }
                                        )
                                        Spacer(Modifier.height(12.dp))
                                    }
                                }
                            }
                            
                            // Bottom Spacer
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }

                1 -> {
                    // "My QR" Tab for Student
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showMyQrDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Tap to show your QR Code",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                "Present this to your teacher for attendance",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                2 -> {
                    // History Tab for Student
                    StudentHistoryScreen(
                        navController = navController,
                        studentId = studentId,
                        onLogout = onLogout
                    )
                }
            }
        }

        // Floating Navigation Bar for Student
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(50),
            elevation = 16.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home Tab
                CustomTabItem(
                    icon = Icons.Default.Dashboard,
                    label = "Home",
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 }
                )

                // 2. My QR Tab
                CustomTabItem(
                    icon = Icons.Default.QrCode,
                    label = "My QR",
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 }
                )

                // 3. History Tab
                CustomTabItem(
                    icon = Icons.Default.History,
                    label = "History",
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 }
                )
            }
        }
    }
}

@Composable
fun AdvisoryClassCard(
    grade: String,
    section: String,
    track: String?,
    adviserName: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryBlue, SecondaryTeal)
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Enrolled Advisory Class",
                        style = MaterialTheme.typography.subtitle2.copy(color = Color.White.copy(alpha = 0.8f))
                    )
                    Icon(Icons.Default.School, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Grade $grade - $section",
                    style = MaterialTheme.typography.h5.copy(
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    )
                )
                
                if (!track.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = track,
                            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        tint = Color.White.copy(alpha = 0.8f), 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Adviser: $adviserName",
                        style = MaterialTheme.typography.body2.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                }
            }
        }
    }
}

@Composable
fun ClassCard(
    subjectClass: com.attendifyplus.data.local.entities.SubjectClassEntity,
    status: String,
    teacherName: String,
    onClick: () -> Unit
) {
    val statusColor = when (status.lowercase()) {
        "present" -> SuccessGreen
        "late" -> WarningYellow
        "absent" -> Color.Red
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
    }
    
    val statusText = when (status.lowercase()) {
        "unmarked" -> "Not Marked"
        else -> status.replaceFirstChar { it.uppercase() }
    }
    
    val statusBg = statusColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Initial
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = subjectClass.subjectName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectClass.subjectName,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = teacherName,
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${subjectClass.gradeLevel} - ${subjectClass.section}",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f))
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Status Chip
            Surface(
                color = statusBg,
                shape = PillShape
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
