package com.attendifyplus.ui.attendance

// Teacher Dashboard Screen Container
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import java.net.URLEncoder
import org.koin.androidx.compose.getViewModel
import kotlinx.coroutines.launch

// Import shared composables
import com.attendifyplus.ui.attendance.CustomTabItem
import com.attendifyplus.ui.attendance.ScanTabItem

@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit = {},
    viewModel: TeacherDashboardViewModel = getViewModel()
) {
    // Use rememberSaveable to retain tab state across navigation backstack operations
    var currentTab by rememberSaveable { mutableStateOf(0) }
    var showAdvisoryDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe Daily Status
    val dailyStatus by viewModel.dailyStatus.collectAsState()
    val isCalendarConfigured by viewModel.isCalendarConfigured.collectAsState()
    // Observe User Name
    val userName by viewModel.userName.collectAsState()
    
    // Determine if attendance actions (Scan/Manual) are enabled
    val isAttendanceEnabled = when {
        dailyStatus.contains("suspended", ignoreCase = true) -> false
        dailyStatus.contains("cancelled", ignoreCase = true) -> false
        dailyStatus.contains("holiday", ignoreCase = true) -> false
        dailyStatus.contains("no class", ignoreCase = true) -> false
        else -> true
    }
    
    val disabledReason = "Attendance actions are disabled: $dailyStatus"
    
    // System Bar Icon Control
    // Tab 0 (Home): Dark Bg -> White Icons (useDarkIcons = false)
    // Tab 1 (Scan): Dark Bg -> White Icons (false)
    // Tab 2 (Tools): White Bg -> Black Icons (true)
    // Tab 3 (My Class): White Bg -> Black Icons (true)
    
    val useDarkIcons = currentTab == 2 || currentTab == 3
    SetSystemBarIcons(useDarkIcons = useDarkIcons)

    // Intercept Back Press
    BackHandler(enabled = true) {
        if (currentTab != 0) {
            currentTab = 0
        } else {
            showExitDialog = true
        }
    }

    if (showAdvisoryDialog) {
        AdvisoryDetailsDialog(onDismiss = { showAdvisoryDialog = false })
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

    // Root Container with Scaffold for Snackbar
    Scaffold(
        scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
        snackbarHost = { 
            SnackbarHost(it) { data ->
                Snackbar(
                    snackbarData = data,
                    backgroundColor = Color(0xFF333333),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 100.dp) // Lift above bottom bar
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colors.background)
                    .navigationBarsPadding() // Fix: Ensure bottom nav is not covered by system nav
            ) {
                // Content Layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (currentTab == 1) 0.dp else 88.dp) // Remove padding for scanner
                ) {
                    when (currentTab) {
                        0 -> DashboardScreen(
                            navController = navController,
                            role = "teacher", // Fixed role for TeacherDashboardScreen
                            onTeachers = { /* Teachers don't manage teachers */ },
                            // Pass user name for personalized greeting
                            userName = userName 
                        )

                        1 -> {
                            // QR Scanner Tab for Teacher
                            if (isAttendanceEnabled) {
                                QRAttendanceScreen(
                                    navController = navController,
                                    onBack = { currentTab = 0 }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(disabledReason, color = MaterialTheme.colors.onBackground)
                                }
                            }
                        }

                        2 -> {
                            // Tools Tab for Teacher (Renamed from Manual)
                            if (isAttendanceEnabled) {
                                ManualAttendanceScreen(navController = navController, onLogout = onLogout)
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(disabledReason, color = MaterialTheme.colors.onBackground)
                                }
                            }
                        }

                        3 -> {
                            // My Class Tab (Newly Added)
                            ManageClassesScreen(
                                navController = navController
                            )
                        }
                    }
                }

                // Floating Navigation Bar for Teacher (Hidden in Scanner Tab)
                if (currentTab != 1) {
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

                            // 2. Scan Tab (Center)
                            Box(
                                modifier = if (!isAttendanceEnabled) {
                                    Modifier.clickable {
                                        scope.launch { snackbarHostState.showSnackbar(disabledReason) }
                                    }
                                } else {
                                    Modifier
                                }
                            ) {
                                ScanTabItem(
                                    selected = currentTab == 1,
                                    enabled = isAttendanceEnabled, // This makes it gray and non-clickable internally
                                    onClick = { 
                                        if (isAttendanceEnabled) currentTab = 1 
                                    }
                                )
                            }

                            // 3. My Class Tab (Newly Added)
                            CustomTabItem(
                                icon = Icons.Default.Class, 
                                label = "My Class", 
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 }
                            )

                            // 4. Tools Tab (Renamed from Manual)
                            Box(
                                modifier = if (!isAttendanceEnabled) {
                                    Modifier.clickable {
                                        scope.launch { snackbarHostState.showSnackbar(disabledReason) }
                                    }
                                } else {
                                    Modifier
                                }
                            ) {

                                CustomTabItem(
                                    icon = Icons.Default.Build, // Changed icon to Build/Tools
                                    label = "Tools", // Renamed from Manual
                                    selected = currentTab == 2,
                                    enabled = isAttendanceEnabled,
                                    onClick = { 
                                        if (isAttendanceEnabled) currentTab = 2 
                                    }
                                )
                            }
                        }
                    }
                }

                // Blocking Overlay for Unconfigured Calendar
                if (!isCalendarConfigured) {
                    SetupPromptOverlay(
                        onNavigate = { navController.navigate("academic_periods") }
                    )
                }
            }
        }
    )
}
