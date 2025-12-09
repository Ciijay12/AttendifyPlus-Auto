package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.StandardScreen
import com.attendifyplus.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel

@Composable
fun StudentExportScreen(
    navController: NavController,
    viewModel: AdminStudentManagementViewModel = getViewModel()
) {
    val context = LocalContext.current
    val advisoryClasses by viewModel.advisoryClasses.collectAsState()
    val scaffoldState = rememberScaffoldState()

    // State
    var selectedClass by remember { mutableStateOf<AdvisoryClassOption?>(null) }
    var exportAll by remember { mutableStateOf(false) }
    var exportAllType by remember { mutableStateOf<String?>(null) } // "JHS" or "SHS"
    
    // Status
    val exportStatus by viewModel.exportStatus.collectAsState()
    var showStatusDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    
    // Listen for export status updates
    LaunchedEffect(exportStatus) {
        exportStatus?.let { message ->
            statusMessage = message
            showStatusDialog = true
            if (message.contains("Success", ignoreCase = true)) {
                delay(3000)
                viewModel.clearExportStatus()
                showStatusDialog = false
                navController.popBackStack()
            }
        }
    }

    // Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            val filter = if (exportAll) "All_$exportAllType" else "${selectedClass?.grade}-${selectedClass?.section}"
            viewModel.exportStudents(it, context, filter)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Export Students", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colors.onSurface)
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                contentColor = MaterialTheme.colors.onSurface
            )
        }
    ) { padding ->
        StandardScreen(modifier = Modifier.padding(padding)) {
             Column {
                Text(
                    "Choose Class to Export",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onBackground
                )
                Text(
                    "Select a specific advisory class or export all.",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(24.dp))

                // Option to Export All - JHS
                GradientActionCard(
                    title = "Export All JHS Students",
                    subtitle = "List of all Grade 7-10 students",
                    icon = Icons.Default.Groups,
                    gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2)),
                    onClick = {
                        exportAll = true
                        exportAllType = "JHS"
                        exportLauncher.launch("All_JHS_Students_${System.currentTimeMillis()}.csv")
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Option to Export All - SHS
                GradientActionCard(
                    title = "Export All SHS Students",
                    subtitle = "List of all Grade 11-12 students",
                    icon = Icons.Default.Groups,
                    gradientColors = listOf(Color(0xFF26A69A), Color(0xFF00695C)),
                    onClick = {
                        exportAll = true
                        exportAllType = "SHS"
                        exportLauncher.launch("All_SHS_Students_${System.currentTimeMillis()}.csv")
                    }
                )

                Spacer(Modifier.height(32.dp))
                Text("Specific Advisory Classes", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                Spacer(Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Group by Grade
                    val grouped = advisoryClasses.groupBy { it.grade }
                    
                    // Sort grades: 7, 8, 9, 10, 11, 12
                    grouped.keys.sortedBy { it.toIntOrNull() ?: 99 }.forEach { grade ->
                        item {
                            Text("Grade $grade", fontWeight = FontWeight.Bold, color = PrimaryBlue, modifier = Modifier.padding(vertical = 4.dp))
                        }
                        items(grouped[grade] ?: emptyList()) { advisoryClass ->
                            AdvisoryClassSelectionCard(
                                advisoryClass = advisoryClass,
                                onClick = {
                                    selectedClass = advisoryClass
                                    exportAll = false
                                    exportLauncher.launch("Students_Grade${advisoryClass.grade}_${advisoryClass.section}_${System.currentTimeMillis()}.csv")
                                }
                            )
                        }
                    }
                }
             }
        }
    }
    
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { 
                showStatusDialog = false 
                viewModel.clearExportStatus()
            },
            title = { Text("Export Status") },
            text = { Text(statusMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showStatusDialog = false 
                    viewModel.clearExportStatus()
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AdvisoryClassSelectionCard(
    advisoryClass: AdvisoryClassOption,
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
                    text = advisoryClass.section.firstOrNull()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(advisoryClass.section, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colors.onSurface)
                Text("Adviser: ${advisoryClass.teacherName}", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                if (advisoryClass.track != null) {
                    Text(advisoryClass.track, style = MaterialTheme.typography.caption, color = SecondaryTeal)
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowForwardIos, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
        }
    }
}
