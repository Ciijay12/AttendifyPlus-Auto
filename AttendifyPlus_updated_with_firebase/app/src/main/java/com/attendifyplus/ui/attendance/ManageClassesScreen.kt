package com.attendifyplus.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import org.koin.androidx.compose.getViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ManageClassesScreen(
    navController: NavController,
    viewModel: SubjectClassViewModel = getViewModel()
) {
    // Fix status bar icons (dark icons for light background)
    SetSystemBarIcons(useDarkIcons = true)

    val classes by viewModel.classes.collectAsState()
    val availableSections by viewModel.availableSections.collectAsState()

    var classToEdit by remember { mutableStateOf<SubjectClassEntity?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }

    // Confirm Delete Dialog State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var classToDelete by remember { mutableStateOf<SubjectClassEntity?>(null) }

    val premiumGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryBlue, SecondaryTeal)
    )

    // Form State
    var subjectName by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var trackAndStrand by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        subjectName = ""
        gradeLevel = ""
        section = ""
        trackAndStrand = null
        classToEdit = null
    }

    fun populateForm(item: SubjectClassEntity) {
        classToEdit = item
        subjectName = item.subjectName
        gradeLevel = item.gradeLevel
        section = item.section
        trackAndStrand = item.trackAndStrand
    }

    fun openAddDialog() {
        resetForm()
        showAddEditDialog = true
    }

    fun openEditDialog(item: SubjectClassEntity) {
        populateForm(item)
        showAddEditDialog = true
    }

    if (showDeleteDialog && classToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                classToDelete = null
            },
            title = { Text("Delete Class", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
            text = { Text("Are you sure you want to delete ${classToDelete?.subjectName} (${classToDelete?.gradeLevel}-${classToDelete?.section})? This action cannot be undone.", color = MaterialTheme.colors.onSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        classToDelete?.let { viewModel.deleteClass(it) }
                        showDeleteDialog = false
                        classToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        classToDelete = null
                    }
                ) {
                    Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    if (showAddEditDialog) {
        val isShs = gradeLevel == "11" || gradeLevel == "12"
        
        // Compute available grades from the sections
        val availableGrades = remember(availableSections) {
            availableSections.map { it.grade }.distinct().sortedBy { it.toIntOrNull() ?: 99 }
        }
        
        // Filter sections based on selected grade
        val filteredSections = remember(availableSections, gradeLevel) {
            availableSections.filter { it.grade == gradeLevel }
        }

        var gradeExpanded by remember { mutableStateOf(false) }
        var sectionExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = if (classToEdit == null) "Add New Class" else "Edit Class",
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(Modifier.height(24.dp))

                    Text("Subject", style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        placeholder = { Text("e.g. Mathematics", color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            backgroundColor = MaterialTheme.colors.surface,
                            textColor = MaterialTheme.colors.onSurface
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Grade Level", style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(4.dp))
                            
                            ExposedDropdownMenuBox(
                                expanded = gradeExpanded,
                                onExpandedChange = { gradeExpanded = !gradeExpanded }
                            ) {
                                OutlinedTextField(
                                    value = gradeLevel,
                                    onValueChange = { },
                                    readOnly = true,
                                    placeholder = { Text("Select") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                        backgroundColor = MaterialTheme.colors.surface,
                                        textColor = MaterialTheme.colors.onSurface
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = gradeExpanded,
                                    onDismissRequest = { gradeExpanded = false }
                                ) {
                                    if (availableGrades.isEmpty()) {
                                        DropdownMenuItem(onClick = { }) {
                                            Text("No grades configured", color = Color.Gray)
                                        }
                                    } else {
                                        availableGrades.forEach { grade ->
                                            DropdownMenuItem(onClick = {
                                                gradeLevel = grade
                                                section = "" // Reset section when grade changes
                                                trackAndStrand = null
                                                gradeExpanded = false
                                            }) {
                                                Text("Grade $grade")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Section", style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(4.dp))
                            
                            ExposedDropdownMenuBox(
                                expanded = sectionExpanded,
                                onExpandedChange = { sectionExpanded = !sectionExpanded }
                            ) {
                                OutlinedTextField(
                                    value = section,
                                    onValueChange = { },
                                    readOnly = true,
                                    placeholder = { Text("Select") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    enabled = gradeLevel.isNotBlank(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                        backgroundColor = MaterialTheme.colors.surface,
                                        textColor = MaterialTheme.colors.onSurface
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = sectionExpanded,
                                    onDismissRequest = { sectionExpanded = false }
                                ) {
                                    if (filteredSections.isEmpty()) {
                                        DropdownMenuItem(onClick = { }) {
                                            Text("No sections found", color = Color.Gray)
                                        }
                                    } else {
                                        filteredSections.forEach { sec ->
                                            DropdownMenuItem(onClick = {
                                                section = sec.section
                                                trackAndStrand = sec.track
                                                sectionExpanded = false
                                            }) {
                                                Column {
                                                    Text(sec.section)
                                                    if (sec.track != null) {
                                                        Text(sec.track, style = MaterialTheme.typography.caption, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isShs) {
                        Spacer(Modifier.height(16.dp))
                        Text("Track and Strand", style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = trackAndStrand ?: "None",
                            onValueChange = { },
                            readOnly = true,
                            enabled = false, // Auto-populated from section
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                                textColor = MaterialTheme.colors.onSurface
                            )
                        )
                        Text(
                            text = "Auto-selected based on section",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { 
                            if (subjectName.isNotBlank() && gradeLevel.isNotBlank() && section.isNotBlank()) {
                                if (classToEdit == null) {
                                    viewModel.addClass(subjectName, gradeLevel, section, null, null, trackAndStrand)
                                } else {
                                    viewModel.updateClass(classToEdit!!.id, subjectName, gradeLevel, section, null, null, trackAndStrand)
                                }
                                showAddEditDialog = false
                                resetForm()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(premiumGradient),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (classToEdit == null) "Add Class" else "Save Changes", 
                                color = Color.White, 
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.button.copy(fontSize = 16.sp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { 
                            showAddEditDialog = false
                            resetForm() 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = PillShape,
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    ) {
                            Text("Cancel")
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colors.background,
        floatingActionButton = {
            if (classes.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Add Class", color = Color.White, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                    onClick = { openAddDialog() },
                    backgroundColor = PrimaryBlue,
                    shape = PillShape
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Classes",
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                Spacer(Modifier.weight(1f))
            }

            if (classes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No classes yet", style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)))
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { openAddDialog() },
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                            modifier = Modifier
                                .width(200.dp)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Create Class", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(classes) { item ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val encSubject = URLEncoder.encode(item.subjectName, "UTF-8")
                                    val encGrade = URLEncoder.encode(item.gradeLevel, "UTF-8")
                                    val encSection = URLEncoder.encode(item.section, "UTF-8")
                                    navController.navigate("class_dashboard/$encSubject/$encGrade/$encSection")
                                },
                            backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PrimaryBlue.copy(alpha = 0.1f),
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = item.subjectName.take(1).uppercase(),
                                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                            )
                                        }
                                    }
                                    
                                    Spacer(Modifier.width(16.dp))
                                    
                                    Column {
                                        Text(
                                            text = item.subjectName,
                                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface),
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = item.trackAndStrand?.let { "${item.gradeLevel} - ${item.section} ($it)" } ?: "${item.gradeLevel} - ${item.section}",
                                            style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                                        )
                                    }
                                }
                                
                                Row {
                                    IconButton(onClick = { openEditDialog(item) }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(onClick = { 
                                        classToDelete = item
                                        showDeleteDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { 
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
