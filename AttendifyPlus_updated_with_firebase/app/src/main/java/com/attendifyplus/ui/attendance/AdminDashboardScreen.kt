package com.attendifyplus.ui.attendance

// Refactored Admin Dashboard
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    role: String,
    onLogout: () -> Unit = {},
    viewModel: AdminDashboardViewModel = getViewModel()
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var isSheetOpen by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val isCalendarConfigured by viewModel.isCalendarConfigured.collectAsState()
    
    // System Bar Icon Control
    // Tab 0 (Home): Dark Bg -> White Icons (false)
    // Tab 1 (Teachers): White Bg -> Black Icons (true)
    // Tab 2 (Creds): White Bg -> Black Icons (true)
    // Tab 3 (Config): White Bg -> Black Icons (true)
    
    val useDarkIcons = selectedTab != 0
    SetSystemBarIcons(useDarkIcons = useDarkIcons)

    // Intercept Back Press: Go to Home tab if not there, else show exit dialog
    BackHandler(enabled = true) {
        if (selectedTab != 0) {
            selectedTab = 0
        } else {
            showExitDialog = true
        }
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

    // Root Container with Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            // Removed statusBarsPadding() from here to allow wavy header to be flush
            .navigationBarsPadding()
    ) {
        // Content Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
             when (selectedTab) {
                0 -> {
                    // Ensure sheet state is reset when not on the teachers tab
                    SideEffect { isSheetOpen = false }
                    Box(Modifier.padding(bottom = 88.dp).fillMaxSize()) {
                        // Pass onLogout explicitly to AdminDashboard
                        AdminDashboard(
                            navController = navController,
                            onTeachers = { selectedTab = 1 },
                            role = role,
                            onLogout = onLogout
                        )
                    }
                }
                1 -> TeacherListScreen(
                    onBack = { selectedTab = 0 },
                    onTeacherClick = { teacherId -> navController.navigate("teacher_detail/$teacherId") },
                    onSheetVisibilityChange = { isVisible -> isSheetOpen = isVisible }
                )
                2 -> {
                    SideEffect { isSheetOpen = false }
                    // Added statusBarsPadding here for other tabs
                    Box(Modifier.padding(bottom = 88.dp).fillMaxSize().statusBarsPadding()) {
                        TeacherCredentialsScreen(navController = navController)
                    }
                }
                3 -> {
                    SideEffect { isSheetOpen = false }
                    // Added statusBarsPadding here for other tabs
                    Box(Modifier.padding(bottom = 88.dp).fillMaxSize().statusBarsPadding()) {
                        AdminConfigurationScreen(navController = navController, onLogout = onLogout)
                    }
                }
            }
        }

        // Removed Logout Button from Top End

        // Floating Navigation Bar
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(64.dp)
                .zIndex(if (isSheetOpen) -1f else 1f), // Send to back when sheet is open
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
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )

                // 2. Teachers Tab
                CustomTabItem(
                    icon = Icons.Default.Person,
                    label = "Teachers",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )

                // 3. Credentials Tab
                CustomTabItem(
                    icon = Icons.Default.Lock,
                    label = "Creds",
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )

                // 4. Config Tab
                CustomTabItem(
                    icon = Icons.Default.Settings,
                    label = "Config",
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }

        // Blocking Overlay for Unconfigured Calendar
        if (!isCalendarConfigured && selectedTab != 3) {
            SetupPromptOverlay(
                onNavigate = { selectedTab = 3 }
            )
        }
    }
}
