package com.attendifyplus.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.TeacherEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import org.koin.androidx.compose.getViewModel

@Composable
fun AdminGradeManagementScreen(
    navController: NavController,
    viewModel: AdminStudentManagementViewModel = getViewModel()
) {
    SetSystemBarIcons(useDarkIcons = MaterialTheme.colors.isLight)
    
    val advisoryClasses by viewModel.advisoryClasses.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val enabledTracks by viewModel.enabledTracks.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Junior High School", "Senior High School")

    // Dialog State
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showEditSectionDialog by remember { mutableStateOf(false) }
    var showTrackSettingsDialog by remember { mutableStateOf(false) }
    var selectedGradeForAdd by remember { mutableStateOf("7") }
    
    // Edit State
    var sectionToEdit by remember { mutableStateOf<AdvisoryClassOption?>(null) }

    if (showAddSectionDialog) {
        AddEditSectionDialog(
            grade = selectedGradeForAdd,
            allTeachers = allTeachers,
            currentAdvisers = advisoryClasses,
            enabledTracks = enabledTracks,
            onDismiss = { showAddSectionDialog = false },
            onSave = { teacher, section, track ->
                viewModel.assignAdviser(teacher, selectedGradeForAdd, section, track)
                showAddSectionDialog = false
            }
        )
    }

    if (showEditSectionDialog && sectionToEdit != null) {
        val grade = sectionToEdit!!.grade
        AddEditSectionDialog(
            grade = grade,
            allTeachers = allTeachers,
            currentAdvisers = advisoryClasses,
            enabledTracks = enabledTracks,
            existingSection = sectionToEdit,
            onDismiss = { 
                showEditSectionDialog = false 
                sectionToEdit = null
            },
            onSave = { teacher, section, track ->
                viewModel.updateSection(
                    oldAdviserId = sectionToEdit!!.teacherId,
                    grade = grade,
                    oldSectionName = sectionToEdit!!.section,
                    newAdviser = teacher,
                    newSectionName = section,
                    newTrack = track
                )
                showEditSectionDialog = false
                sectionToEdit = null
            }
        )
    }

    if (showTrackSettingsDialog) {
        TrackSettingsDialog(
            enabledTracks = enabledTracks,
            onToggleTrack = { track, enabled -> viewModel.toggleTrack(track, enabled) },
            onDismiss = { showTrackSettingsDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade & Track Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showTrackSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Track Settings", tint = MaterialTheme.colors.onSurface)
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                modifier = Modifier.statusBarsPadding()
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            
            // Modern Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = PrimaryBlue,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(PrimaryBlue)
                    )
                },
                divider = { Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if(selectedTab == index) PrimaryBlue else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            ) 
                        },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTab) {
                0 -> GradeLevelsTab( // JHS
                    grades = listOf("7", "8", "9", "10"),
                    advisoryClasses = advisoryClasses,
                    onAddSection = { grade ->
                        selectedGradeForAdd = grade
                        showAddSectionDialog = true
                    },
                    onEditSection = { section ->
                        sectionToEdit = section
                        showEditSectionDialog = true
                    },
                    onRemoveAdviser = { teacherId ->
                        val teacher = allTeachers.find { it.id == teacherId }
                        teacher?.let { viewModel.removeAdviser(it) }
                    }
                )
                1 -> GradeLevelsTab( // SHS
                    grades = listOf("11", "12"),
                    advisoryClasses = advisoryClasses,
                    onAddSection = { grade ->
                        selectedGradeForAdd = grade
                        showAddSectionDialog = true
                    },
                    onEditSection = { section ->
                        sectionToEdit = section
                        showEditSectionDialog = true
                    },
                    onRemoveAdviser = { teacherId ->
                        val teacher = allTeachers.find { it.id == teacherId }
                        teacher?.let { viewModel.removeAdviser(it) }
                    }
                )
            }
        }
    }
}

@Composable
fun GradeLevelsTab(
    grades: List<String>,
    advisoryClasses: List<AdvisoryClassOption>,
    onAddSection: (String) -> Unit,
    onEditSection: (AdvisoryClassOption) -> Unit,
    onRemoveAdviser: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(grades) { grade ->
            val sections = advisoryClasses.filter { it.grade == grade }
            
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp,
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Modern Header with Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryBlue, PrimaryBlue.copy(alpha = 0.7f))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grade $grade",
                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${sections.size} Sections",
                                    style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = Color.White),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (sections.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No sections configured yet.",
                                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)),
                                )
                            }
                        } else {
                            sections.forEachIndexed { index, section ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryBlue.copy(alpha = 0.1f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = section.section.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (section.track != null) "${section.section} (${section.track})" else section.section,
                                                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                                            )
                                            Text(
                                                text = section.teacherName,
                                                style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                                            )
                                        }
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { onEditSection(section) }
                                        ) {
                                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                                        }
                                        IconButton(
                                            onClick = { onRemoveAdviser(section.teacherId) }
                                        ) {
                                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                                if (index < sections.size - 1) {
                                    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = { onAddSection(grade) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue,
                                backgroundColor = Color.Transparent
                            )
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Section")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackSettingsDialog(
    enabledTracks: Set<String>,
    onToggleTrack: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    // Classified Tracks according to SHS Philippines
    val trackGroups = remember {
        listOf(
            "Academic Track" to listOf("ABM", "HUMSS", "STEM", "GAS"),
            "Technical-Vocational-Livelihood (TVL) Track" to listOf("AFA", "HE", "IA", "ICT"),
            "Arts and Design Track" to listOf("Arts and Design"),
            "Sports Track" to listOf("Sports")
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp),
            elevation = 12.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Tracks",
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                }
                
                Text(
                    text = "Toggle the switches to enable or disable tracks/strands available in your school.",
                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    trackGroups.forEach { (category, tracks) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.subtitle2.copy(
                                    fontWeight = FontWeight.Bold, 
                                    color = PrimaryBlue
                                ),
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        
                        items(tracks) { track ->
                            val isEnabled = enabledTracks.contains(track)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = 0.dp,
                                border = BorderStroke(1.dp, if (isEnabled) PrimaryBlue else MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
                                backgroundColor = if (isEnabled) PrimaryBlue.copy(alpha = 0.05f) else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = track,
                                        style = MaterialTheme.typography.subtitle1.copy(
                                            fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isEnabled) PrimaryBlue else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = { onToggleTrack(track, it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PrimaryBlue,
                                            checkedTrackColor = PrimaryBlue.copy(alpha = 0.5f),
                                            uncheckedThumbColor = MaterialTheme.colors.surface,
                                            uncheckedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddEditSectionDialog(
    grade: String,
    allTeachers: List<TeacherEntity>,
    currentAdvisers: List<AdvisoryClassOption>,
    enabledTracks: Set<String>,
    existingSection: AdvisoryClassOption? = null,
    onDismiss: () -> Unit,
    onSave: (TeacherEntity, String, String?) -> Unit
) {
    var sectionName by remember { mutableStateOf(existingSection?.section ?: "") }
    
    // Initialize selected teacher based on edit mode
    val initialTeacher = remember(existingSection) {
        if (existingSection != null) {
            allTeachers.find { it.id == existingSection.teacherId }
        } else {
            null
        }
    }
    
    var selectedTeacher by remember { mutableStateOf<TeacherEntity?>(initialTeacher) }
    
    // Track State Initialization
    val initialTrack = existingSection?.track
    val isMultiTrack = initialTrack?.contains("+") == true
    
    // Multi-select state for Tracks
    var isCombined by remember { mutableStateOf(isMultiTrack) }
    var selectedTracks by remember { 
        mutableStateOf(
            if (isMultiTrack) initialTrack!!.split(" + ").toSet() else emptySet()
        ) 
    }
    var selectedTrackSingle by remember { 
        mutableStateOf(
            if (!isMultiTrack && initialTrack != null) initialTrack else ""
        ) 
    }
    
    var teacherExpanded by remember { mutableStateOf(false) }
    var trackExpanded by remember { mutableStateOf(false) }
    
    val isShs = grade == "11" || grade == "12"
    val targetDepartment = if (isShs) "SHS" else "JHS"

    val currentAdviserIds = currentAdvisers.map { it.teacherId }.toSet()
    
    // Filter teachers based on availability, department, and role
    // Include the current teacher if in edit mode so they appear in the list
    val availableTeachers = allTeachers.filter { teacher ->
        (!currentAdviserIds.contains(teacher.id) || teacher.id == existingSection?.teacherId) && 
        teacher.department == targetDepartment &&
        teacher.role == "adviser"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = 12.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (existingSection != null) "Edit Section" else "Add Section",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                Text(
                    text = "Configure details for Grade $grade",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Track/Strand Selection for SHS
                if (isShs) {
                    // Toggle for Combined Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Checkbox(
                            checked = isCombined,
                            onCheckedChange = { 
                                isCombined = it 
                                // Reset only if switching modes to avoid confusion, or keep logic simple
                                if (it) {
                                    // Switch to combined, clear single
                                    selectedTrackSingle = ""
                                } else {
                                    // Switch to single, clear multiple
                                    selectedTracks = emptySet()
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                        )
                        Text(
                            text = "Combined Strand Section (e.g. ICT + HE)",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface
                        )
                    }

                    if (isCombined) {
                        // Multi-select Dropdown logic
                        ExposedDropdownMenuBox(
                            expanded = trackExpanded,
                            onExpandedChange = { trackExpanded = !trackExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (selectedTracks.isEmpty()) "" else selectedTracks.sorted().joinToString(" + "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Strands to Combine") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trackExpanded) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = MaterialTheme.colors.onSurface,
                                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = trackExpanded,
                                onDismissRequest = { trackExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colors.surface)
                            ) {
                                if (enabledTracks.isEmpty()) {
                                    DropdownMenuItem(onClick = {}) {
                                        Text("No enabled tracks", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                                    }
                                } else {
                                    enabledTracks.sorted().forEach { track ->
                                        val isSelected = selectedTracks.contains(track)
                                        DropdownMenuItem(onClick = {
                                            val newSet = selectedTracks.toMutableSet()
                                            if (isSelected) newSet.remove(track) else newSet.add(track)
                                            selectedTracks = newSet
                                            // Don't close menu to allow multiple selection
                                        }) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = null, // Handled by item click
                                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(track, color = MaterialTheme.colors.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Single Select
                        ExposedDropdownMenuBox(
                            expanded = trackExpanded,
                            onExpandedChange = { trackExpanded = !trackExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedTrackSingle,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Strand/Track") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trackExpanded) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = MaterialTheme.colors.onSurface,
                                    leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    trailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = trackExpanded,
                                onDismissRequest = { trackExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colors.surface)
                            ) {
                                if (enabledTracks.isEmpty()) {
                                    DropdownMenuItem(onClick = {}) {
                                        Text("No enabled tracks", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                                    }
                                } else {
                                    enabledTracks.sorted().forEach { track ->
                                        DropdownMenuItem(onClick = {
                                            selectedTrackSingle = track
                                            trackExpanded = false
                                        }) {
                                            Text(track, color = MaterialTheme.colors.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Section Name (e.g. Amber)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = !teacherExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (selectedTeacher == null) "" else "${selectedTeacher!!.lastName}, ${selectedTeacher!!.firstName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Adviser") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onSurface,
                            trailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = teacherExpanded,
                        onDismissRequest = { teacherExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colors.surface)
                    ) {
                        if (availableTeachers.isEmpty()) {
                            DropdownMenuItem(onClick = {}) {
                                Text("No available $targetDepartment advisers. Check Teachers Directory.", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                            }
                        } else {
                            availableTeachers.forEach { teacher ->
                                DropdownMenuItem(onClick = {
                                    selectedTeacher = teacher
                                    teacherExpanded = false
                                }) {
                                    Text("${teacher.lastName}, ${teacher.firstName}", color = MaterialTheme.colors.onSurface)
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                    Button(
                        onClick = { 
                            if (selectedTeacher != null && sectionName.isNotBlank()) {
                                var finalTrack: String? = null
                                if (isShs) {
                                    if (isCombined) {
                                        if (selectedTracks.isEmpty()) return@Button // Require at least one
                                        finalTrack = selectedTracks.sorted().joinToString(" + ")
                                    } else {
                                        if (selectedTrackSingle.isBlank()) return@Button
                                        finalTrack = selectedTrackSingle
                                    }
                                }
                                onSave(selectedTeacher!!, sectionName, finalTrack)
                            }
                        },
                        enabled = selectedTeacher != null && sectionName.isNotBlank() && 
                            (!isShs || (isCombined && selectedTracks.isNotEmpty()) || (!isCombined && selectedTrackSingle.isNotBlank())),
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Save Section", color = Color.White)
                    }
                }
            }
        }
    }
}
