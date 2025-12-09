package com.attendifyplus.ui.attendance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import org.koin.androidx.compose.getViewModel

@Composable
fun AdminGradeDetailScreen(
    navController: NavController,
    grade: String,
    viewModel: AdminStudentManagementViewModel = getViewModel()
) {
    SetSystemBarIcons(useDarkIcons = true)
    // val context = LocalContext.current // Removed unused variable

    val students by viewModel.filteredStudents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val advisoryClasses by viewModel.advisoryClasses.collectAsState()
    val enabledTracks by viewModel.enabledTracks.collectAsState() 

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    
    val isSeniorHigh = grade == "11" || grade == "12"
    val displayTracks = if (isSeniorHigh) enabledTracks.sorted() else emptyList()

    // Initialize selection if no track is selected yet
    LaunchedEffect(grade) {
        viewModel.selectGrade(grade)
        if (isSeniorHigh && (viewModel.selectedTrack.value == "All" || viewModel.selectedTrack.value !in enabledTracks)) {
            if (displayTracks.isNotEmpty()) {
                viewModel.selectTrack(displayTracks.first())
            }
        }
    }
    
    LaunchedEffect(displayTracks) {
        if (isSeniorHigh && displayTracks.isNotEmpty() && viewModel.selectedTrack.value !in displayTracks) {
             viewModel.selectTrack(displayTracks.first())
        }
    }

    val filteredAdvisoryClasses = remember(advisoryClasses, grade, selectedTrack) {
        var filtered = advisoryClasses.filter { it.grade == grade }
        if (isSeniorHigh && selectedTrack.isNotBlank() && selectedTrack != "All") {
            filtered = filtered.filter { 
                it.track == selectedTrack || it.track?.contains(selectedTrack) == true 
            }
        }
        filtered
    }

    val studentsBySection = students.groupBy { it.section }
    val sortedSections = studentsBySection.keys.sorted()

    if (showAddDialog) {
        AddStudentPremiumDialog(
            advisoryClasses = filteredAdvisoryClasses,
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
                val updated = studentToEdit!!.copy(
                    id = id, 
                    firstName = first, 
                    lastName = last, 
                    grade = advisory.grade, 
                    section = advisory.section
                )
                viewModel.updateStudent(updated)
                showEditDialog = false
                studentToEdit = null
            },
            onGenerateId = { studentToEdit!!.id },
            initialStudent = studentToEdit
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade $grade Students", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
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
        Column(modifier = Modifier.padding(padding)) {
            
            if (isSeniorHigh && displayTracks.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 120.dp)
                ) {
                    items(displayTracks) { track ->
                        FilterChip(
                            selected = selectedTrack == track,
                            label = track,
                            onClick = { viewModel.selectTrack(track) },
                            isSmall = true
                        )
                    }
                }
                Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search student in Grade $grade...", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) },
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

            if (students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, tint = MaterialTheme.colors.onBackground.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matches found" else "No students in Grade $grade for $selectedTrack", 
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f), 
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        items(students) { student ->
                            StudentListItemPremium(
                                student = student,
                                onEdit = { 
                                    studentToEdit = it
                                    showEditDialog = true
                                },
                                onDelete = { viewModel.archiveStudent(it) }
                            )
                        }
                    } else {
                         sortedSections.forEach { section ->
                             val sectionStudents = studentsBySection[section] ?: emptyList()
                             item {
                                 Text(
                                     text = "Section: $section",
                                     style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue),
                                     modifier = Modifier.padding(bottom = 8.dp)
                                 )
                             }
                             items(sectionStudents) { student ->
                                 StudentListItemPremium(
                                     student = student,
                                     onEdit = { 
                                         studentToEdit = it
                                         showEditDialog = true
                                     },
                                     onDelete = { viewModel.archiveStudent(it) }
                                 )
                                 Spacer(Modifier.height(8.dp))
                             }
                             item {
                                 Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                             }
                         }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}
