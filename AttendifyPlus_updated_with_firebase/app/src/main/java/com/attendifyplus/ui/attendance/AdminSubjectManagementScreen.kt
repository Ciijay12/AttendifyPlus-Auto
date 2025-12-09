package com.attendifyplus.ui.attendance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.AdminSubjectEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel

@Composable
fun AdminSubjectManagementScreen(
    navController: NavController,
    viewModel: AdminSubjectManagementViewModel = getViewModel()
) {
    SetSystemBarIcons(useDarkIcons = MaterialTheme.colors.isLight)
    val context = LocalContext.current
    
    val subjects by viewModel.subjects.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    var selectedGrade by remember { mutableStateOf<String?>(null) } // Null means no grade selected (show grid)

    // Dialog State
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showImportHelpDialog by remember { mutableStateOf(false) }

    // CSV Import Launcher
    val csvImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importSubjectsFromCsv(context, it) }
    }
    
    val scaffoldState = rememberScaffoldState()
    
    LaunchedEffect(importStatus) {
        importStatus?.let {
            scaffoldState.snackbarHostState.showSnackbar(it)
            viewModel.clearImportStatus()
        }
    }

    if (showAddSubjectDialog && selectedGrade != null) {
        AddSubjectDialog(
            initialGrade = selectedGrade!!,
            onDismiss = { showAddSubjectDialog = false },
            onSave = { subject ->
                viewModel.addSubject(subject)
                showAddSubjectDialog = false
            }
        )
    }
    
    if (showImportHelpDialog) {
        ImportSubjectHelpDialog(
            onDismiss = { showImportHelpDialog = false },
            onProceed = {
                showImportHelpDialog = false
                csvImportLauncher.launch("*/*")
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
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
                    onClick = { 
                        if (selectedGrade != null) {
                            selectedGrade = null // Go back to grid view
                        } else {
                            navController.popBackStack() 
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (selectedGrade == null) "Subject Management" else "Grade $selectedGrade Subjects",
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                    )
                    if (selectedGrade == null) {
                         Text(
                            text = "Select a grade level to manage subjects",
                            style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                        )
                    }
                }
                
                if (selectedGrade == null) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showImportHelpDialog = true }) {
                        Icon(Icons.Default.Upload, contentDescription = "Import CSV", tint = PrimaryBlue)
                    }
                }
            }

            if (selectedGrade == null) {
                // Grade Grid View
                val grades = listOf("7", "8", "9", "10", "11", "12")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(grades) { grade ->
                        val count = subjects.count { it.gradeLevel == grade }
                        GradeManagementCard(
                            grade = grade,
                            subjectCount = count,
                            onClick = { selectedGrade = grade }
                        )
                    }
                }
            } else {
                // Subject List View for Selected Grade
                val filteredSubjects = subjects.filter { it.gradeLevel == selectedGrade }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    if (filteredSubjects.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No subjects added for Grade $selectedGrade", color = Color.Gray)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddSubjectDialog = true },
                                    shape = PillShape,
                                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add First Subject", color = Color.White)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredSubjects) { subject ->
                                SubjectCard(
                                    subject = subject,
                                    onDelete = { viewModel.deleteSubject(subject) }
                                )
                            }
                            // Spacer for FAB
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                        
                        // FAB
                        FloatingActionButton(
                            onClick = { showAddSubjectDialog = true },
                            backgroundColor = PrimaryBlue,
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Subject")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradeManagementCard(
    grade: String,
    subjectCount: Int,
    onClick: () -> Unit
) {
    val isShs = grade == "11" || grade == "12"
    val color = if (isShs) Color(0xFFFF9F43) else PrimaryBlue
    
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = color)
                    )
                }
            }
            
            Column {
                Text(
                    text = if (isShs) "Senior High" else "Junior High",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
                Text(
                    text = "Grade $grade",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Spacer(Modifier.height(4.dp))
                 Text(
                    text = "$subjectCount Subjects",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium, color = color)
                )
            }
        }
    }
}

@Composable
fun SubjectCard(
    subject: AdminSubjectEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                val details = mutableListOf<String>()
                if (subject.type != "Core") details.add(subject.type)
                if (subject.semester != null) details.add(subject.semester + " Sem")
                if (subject.track != null) details.add(subject.track)
                
                if (details.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = details.joinToString(" • "),
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddSubjectDialog(
    initialGrade: String,
    onDismiss: () -> Unit,
    onSave: (AdminSubjectEntity) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf(initialGrade) }
    var semester by remember { mutableStateOf("1st") } // Default 1st
    var track by remember { mutableStateOf("All") } // Default All for SHS
    var type by remember { mutableStateOf("Core") }

    val isShs = gradeLevel == "11" || gradeLevel == "12"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add Subject - Grade $gradeLevel", style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue))
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Grade Level Dropdown (Optional, usually pre-selected)
                // Semester Dropdown (Only for SHS)
                if (isShs) {
                    Text("Semester", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    Row {
                        RadioButton(selected = semester == "1st", onClick = { semester = "1st" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
                        Text("1st", modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
                        RadioButton(selected = semester == "2nd", onClick = { semester = "2nd" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
                        Text("2nd", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                    Spacer(Modifier.height(8.dp))
                    
                    // Type Dropdown
                    Text("Type", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    Row {
                        RadioButton(selected = type == "Core", onClick = { type = "Core" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
                        Text("Core", modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp))
                        RadioButton(selected = type == "Applied", onClick = { type = "Applied" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
                        Text("Applied", modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp))
                         RadioButton(selected = type == "Specialized", onClick = { type = "Specialized" }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
                        Text("Specialized", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Button(
                        onClick = {
                            if (subjectName.isNotBlank()) {
                                val newSubject = AdminSubjectEntity(
                                    subjectName = subjectName,
                                    gradeLevel = gradeLevel,
                                    semester = if (isShs) semester else null,
                                    type = if (isShs) type else "Core",
                                    track = if (isShs && type == "Specialized") track else null // Simplify for now
                                )
                                onSave(newSubject)
                            }
                        },
                        enabled = subjectName.isNotBlank(),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ImportSubjectHelpDialog(onDismiss: () -> Unit, onProceed: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Subjects from CSV", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
        text = {
            Column {
                Text("Format your CSV file with the following columns:", color = MaterialTheme.colors.onSurface)
                Spacer(Modifier.height(8.dp))
                Card(
                    backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Subject Name, Grade Level, Semester (Optional), Track (Optional), Type (Optional)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colors.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Example 1: Math 7, 7", fontSize = 12.sp, color = MaterialTheme.colors.onSurface)
                        Text("Example 2: Pre-Calculus, 11, 1st, STEM, Specialized", fontSize = 12.sp, color = MaterialTheme.colors.onSurface)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Note: This will add new subjects. Duplicates (same name and grade) might be skipped or updated depending on configuration.", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onProceed, shape = PillShape, colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)) {
                Text("Select CSV File", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            }
        },
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.surface
    )
}
