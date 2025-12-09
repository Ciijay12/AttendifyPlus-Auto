package com.attendifyplus.ui.attendance

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.SuccessGreen
import org.koin.androidx.compose.getViewModel
import java.net.URLEncoder

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
    val importStatus by viewModel.importStatus.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredStudents by viewModel.filteredStudents.collectAsState()
    val advisoryClasses by viewModel.advisoryClasses.collectAsState()
    
    val scaffoldState = rememberScaffoldState()

    // UI State
    var showImportHelpDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var departmentSelection by remember { mutableStateOf("JHS") } // Default JHS

    // Helper to extract grade number robustly
    fun parseGrade(gradeStr: String): Int {
        return gradeStr.filter { it.isDigit() }.toIntOrNull() ?: 0
    }

    // Filter Advisory Classes based on Department
    val filteredAdvisoryClasses = remember(advisoryClasses, departmentSelection) {
        val isJHS = departmentSelection == "JHS"
        advisoryClasses.filter { 
            val g = parseGrade(it.grade)
            // JHS: 7-10 (and potentially lower if elementary, but usually 7-10)
            // SHS: 11-12
            // If g is 0 (parsing failed), we might defaulting to JHS or handle it safely.
            // Let's assume 0 goes to JHS for now.
            if (isJHS) g <= 10 else g > 10
        }
    }

    // Group by Grade for better organization
    val groupedAdvisoryClasses = remember(filteredAdvisoryClasses) {
        filteredAdvisoryClasses.groupBy { it.grade }
            .toSortedMap(compareBy { parseGrade(it) })
    }

    // CSV Import Launcher
    val csvImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importStudentsFromCsv(context, it) }
    }
    
    // Show import status in Snackbar
    LaunchedEffect(importStatus) {
        importStatus?.let { message ->
            scaffoldState.snackbarHostState.showSnackbar(message)
            viewModel.clearImportStatus()
        }
    }
    
    // Show export status in Snackbar
    LaunchedEffect(exportStatus) {
        exportStatus?.let { message ->
            scaffoldState.snackbarHostState.showSnackbar(message)
            viewModel.clearExportStatus()
        }
    }

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
            onGenerateId = { studentToEdit!!.id }, 
            initialStudent = studentToEdit
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        // REMOVED FAB HERE
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
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Advisory Classes",
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                    )
                    Text(
                        text = "Manage student sections",
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                }
            }
            
            // 1. Global Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search by name or ID...", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
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
                // 2b. Advisory Class List Mode
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Removed Import/Export Button Row as requested

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

                    if (groupedAdvisoryClasses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No advisory classes found for this department.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            groupedAdvisoryClasses.forEach { (grade, classes) ->
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = PrimaryBlue.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = "Grade $grade",
                                                style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                                    }
                                }
                                items(classes) { advisoryClass ->
                                    AdvisoryClassCard(
                                        advisoryClass = advisoryClass,
                                        onClick = {
                                            val encodedSection = URLEncoder.encode(advisoryClass.section, "UTF-8")
                                            navController.navigate("admin_advisory_detail/${advisoryClass.grade}/$encodedSection")
                                        }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvisoryClassCard(
    advisoryClass: AdvisoryClassOption,
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
            // Icon Placeholder using initials of section
            Surface(
                shape = RoundedCornerShape(12.dp),
                // Fix color logic to use safe parsing
                color = if (advisoryClass.grade.filter { it.isDigit() }.toIntOrNull()?.let { it > 10 } == true) Color(0xFFFF9F43).copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = advisoryClass.section.take(1).uppercase(),
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold, 
                            color = if (advisoryClass.grade.filter { it.isDigit() }.toIntOrNull()?.let { it > 10 } == true) Color(0xFFFF9F43) else PrimaryBlue
                        )
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = advisoryClass.section,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = advisoryClass.teacherName,
                        style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                }
                
                if (advisoryClass.track != null) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        color = SecondaryTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = advisoryClass.track,
                            style = MaterialTheme.typography.caption.copy(color = SecondaryTeal, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
        }
    }
}
