package com.attendifyplus.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminConfigurationScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    // Standard Top App Bar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
            .background(MaterialTheme.colors.background)
    ) {
         // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp), // Bottom padding for nav bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Management Section
            item {
                SectionHeader("Data Management")
            }
            item {
                // Featured Card for Import/Export
                GradientConfigCard(
                    title = "Import & Export",
                    description = "Manage students, subjects, attendance",
                    icon = Icons.Default.SwapVert,
                    gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2)),
                    onClick = { navController.navigate("config_import_export") }
                )
            }

            // General Section
            item {
                SectionHeader("General Settings")
            }
            item {
                ConfigOptionCardPremium(
                    title = "School Calendar",
                    description = "Configure dates & holidays",
                    icon = Icons.Default.DateRange,
                    iconColor = SecondaryTeal,
                    onClick = { navController.navigate("school_calendar") }
                )
            }
            item {
                ConfigOptionCardPremium(
                    title = "Academic Periods",
                    description = "Manage quarters & semesters",
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFFFFA726), // Orange
                    onClick = { navController.navigate("academic_periods") }
                )
            }
            item {
                ConfigOptionCardPremium(
                    title = "Track Configuration",
                    description = "Manage available SHS tracks",
                    icon = Icons.Default.ListAlt,
                    iconColor = Color(0xFF7E57C2), // Deep Purple
                    onClick = { navController.navigate("track_configuration") }
                )
            }

            // User Management Section
            item {
                SectionHeader("User Management")
            }
            item {
                ConfigOptionCardPremium(
                    title = "Advisory Class Management",
                    description = "Add, edit, or remove students",
                    icon = Icons.Default.Groups,
                    iconColor = PrimaryBlue,
                    onClick = { navController.navigate("admin_student_management") }
                )
            }
            item {
                ConfigOptionCardPremium(
                    title = "Subject Management",
                    description = "Configure subjects for all levels",
                    icon = Icons.Default.Class,
                    iconColor = Color(0xFFAB47BC), // Purple
                    onClick = { navController.navigate("admin_subject_management") }
                )
            }
            
            // App Management
            item {
                SectionHeader("App Management")
            }
             item {
                ConfigOptionCardPremium(
                    title = "Archived Data",
                    description = "View archived records",
                    icon = Icons.Default.Archive,
                    iconColor = Color.Gray,
                    onClick = { navController.navigate("archived_students") }
                )
            }
            
            item {
                // Debug Settings Button (Accidental Tap Proof)
                DebugSettingsButton(onClick = { navController.navigate("debug_settings") })
            }

            item {
                // Logout Card
                ConfigOptionCardPremium(
                    title = "Logout",
                    description = "Sign out of your account",
                    icon = Icons.Default.ExitToApp,
                    iconColor = Color.Red,
                    textColor = Color.Red,
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
fun DebugSettingsButton(onClick: () -> Unit) {
    var tapCount by remember { mutableStateOf(0) }
    // val scope = rememberCoroutineScope() // Removed unused variable
    
    // Reset tap count after 3 seconds of inactivity
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            delay(2000)
            tapCount = 0
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                tapCount++
                if (tapCount >= 5) {
                    onClick()
                    tapCount = 0
                }
            },
        backgroundColor = if (tapCount > 0) MaterialTheme.colors.surface.copy(alpha = 0.9f) else MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (tapCount > 0) Color.Red.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.BugReport, 
                        contentDescription = null, 
                        tint = if (tapCount > 0) Color.Red else Color.Gray, 
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Debug Settings",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Text(
                    text = if (tapCount > 0) "Tap ${5 - tapCount} more times to access" else "Developer options (Hidden)",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle2.copy(
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
            fontSize = 13.sp
        ),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun ConfigOptionCardPremium(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    textColor: Color = MaterialTheme.colors.onSurface,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = textColor)
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
fun GradientConfigCard(
    title: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.body2.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
