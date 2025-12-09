package com.attendifyplus.ui.attendance

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.SuccessGreen
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdminStudentManagementScreen(
    navController: NavController,
    viewModel: AdminStudentManagementViewModel = getViewModel()
) {
    SetSystemBarIcons(useDarkIcons = true)
    val context = LocalContext.current

    // Reset filters when entering this screen (Dashboard mode)
    LaunchedEffect(Unit) {
        viewModel.selectGrade("All")
        viewModel.selectTrack("All")
        viewModel.onSearchQueryChange("")
    }

    // Data State
    val studentCounts by viewModel.studentCounts.collectAsState()
    val gradeCounts by viewModel.gradeCounts.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredStudents by viewModel.filteredStudents.collectAsState()
    val advisoryClasses by viewModel.advisoryClasses.collectAsState()
    
    val scaffoldState = rememberScaffoldState()

    // UI State
    var showImportHelpDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var departmentSelection by remember { mutableStateOf("JHS") } // Default JHS

    // CSV Import Launcher
    val csvImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importStudentsFromCsv(context, it) }
    }
    
    // CSV Export Launcher
    // We don't necessarily need a launcher if we are using the simple share intent method 
    // implemented in viewModel.exportStudentsCsv, but if we want to save directly, we keep it.
    // The previous error was resolved by adding exportStudentsCsv to the VM.
    
    // Show import status in Snackbar
    LaunchedEffect(importStatus) {
        importStatus?.let { message ->
            scaffoldState.snackbarHostState.showSnackbar(message)
            viewModel.clearImportStatus()
        }
    }
    
    // Show export status in Snackbar (if added)
    LaunchedEffect(exportStatus) {
        exportStatus?.let { message ->
            scaffoldState.snackbarHostState.showSnackbar(message)
            viewModel.clearExportStatus()
        }
    }

    // Filter Lists
    val jhsGrades = listOf("7", "8", "9", "10")
    val shsGrades = listOf("11", "12")
    val visibleGrades = if (departmentSelection == "JHS") jhsGrades else shsGrades

    // Back Handler to clear search if active
    BackHandler(enabled = searchQuery.isNotEmpty()) {
        viewModel.onSearchQueryChange("")
    }

    if (showImportHelpDialog) {
        ImportHelpDialog(
            onDismiss = { showImportHelpDialog = false },
            onProceed = {
                showImportHelpDialog = false
                csvImportLauncher.launch("*/*")
            }
        )
    }

    if (showAddDialog) {
        AddStudentPremiumDialog(
            advisoryClasses = advisoryClasses,
            onDismiss = { showAddDialog = false },
            onSave = { id, first, last, advisory ->
                viewModel.addStudent(id, first, last, advisory)
                showAddDialog = false
            },
            onGenerateId = { viewModel.generateStudentId() }
        )
    }

    if (showEditDialog && studentToEdit != null) {
        AddStudentPremiumDialog(
            advisoryClasses = advisoryClasses,
            onDismiss = { 
                showEditDialog = false
                studentToEdit = null
            },
            onSave = { id, first, last, advisory ->
                // Construct updated student entity
                val updatedStudent = studentToEdit!!.copy(
                    id = id,
                    firstName = first,
                    lastName = last,
                    grade = advisory.grade,
                    section = advisory.section
                )
                viewModel.updateStudent(updatedStudent)
                showEditDialog = false
                studentToEdit = null
            },
            onGenerateId = { studentToEdit!!.id }, // Not used for editing but required by signature
            initialStudent = studentToEdit
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            // Always show FAB for adding students
            FloatingActionButton(
                onClick = { showAddDialog = true },
                backgroundColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp), // Matched padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Student Management",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
            }
            
            // 1. Global Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search by name or ID...", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp), // Adjusted vertical padding
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                    textColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Clear, null) } }
                } else null
            )

            if (searchQuery.isNotEmpty()) {
                // 2a. Search Results Mode
                if (filteredStudents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No students found matching \"$searchQuery\"", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredStudents) { student ->
                            StudentListItemPremium(
                                student = student,
                                onEdit = { 
                                    studentToEdit = it
                                    showEditDialog = true
                                },
                                onDelete = { viewModel.archiveStudent(it) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            } else {
                // 2b. Dashboard Mode
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Quick Actions Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showImportHelpDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                            elevation = ButtonDefaults.elevation(2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Import CSV", color = PrimaryBlue)
                        }

                        Button(
                            onClick = { 
                                // Direct export without file picker for this specific quick action, 
                                // OR we can use the file picker launcher.
                                // The previous error was because exportStudentsCsv was called directly but requires context/list.
                                // Let's use the viewModel function we added: exportStudentsCsv(context, list)
                                // But since that function writes to cache and shares intent, we don't need CreateDocument launcher.
                                viewModel.exportStudentsCsv(context, filteredStudents) 
                            }, 
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                            elevation = ButtonDefaults.elevation(2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export CSV", color = PrimaryBlue)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats Overview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatChip(label = "Total", count = studentCounts.total, color = PrimaryBlue, modifier = Modifier.weight(1f))
                        StatChip(label = "JHS", count = studentCounts.jhs, color = SecondaryTeal, modifier = Modifier.weight(1f))
                        StatChip(label = "SHS", count = studentCounts.shs, color = Color(0xFFFFA726), modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(24.dp))

                    // Department Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colors.surface, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DepartmentTab(
                            title = "Junior High",
                            selected = departmentSelection == "JHS",
                            modifier = Modifier.weight(1f),
                            onClick = { departmentSelection = "JHS" }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        DepartmentTab(
                            title = "Senior High",
                            selected = departmentSelection == "SHS",
                            modifier = Modifier.weight(1f),
                            onClick = { departmentSelection = "SHS" }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Grade Cards
                    Text(
                        text = "Browse by Grade",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(visibleGrades) { grade ->
                            val count = gradeCounts[grade] ?: 0
                            GradeCard(
                                grade = grade,
                                studentCount = count,
                                onClick = { navController.navigate("admin_grade_detail/$grade") }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}
