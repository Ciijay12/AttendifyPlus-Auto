package com.attendifyplus.ui.attendance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.util.PrintUtils
import com.attendifyplus.util.QRGenerator
import org.json.JSONObject
import org.koin.androidx.compose.getViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun StudentListScreen(
    viewModel: StudentListViewModel = getViewModel(),
    initialGrade: String? = null,
    initialSection: String? = null,
    onBack: () -> Unit = {}
    // Removed unused external callbacks
) {
    val students by viewModel.students.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Multi-selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedStudents = remember { mutableStateListOf<String>() }

    // Dialog state
    var showQrForStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var showImportHelp by remember { mutableStateOf(false) }

    // Sorting
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter/Search
    var searchQuery by remember { mutableStateOf("") }
    
    // Add/Edit Sheet State
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }

    // File Picker for CSV
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    viewModel.importCsvFromStream(stream)
                }
            }
        }
    )

    val filteredStudents = students.filter {
        // Filter by Search
        (it.firstName.contains(searchQuery, ignoreCase = true) ||
        it.lastName.contains(searchQuery, ignoreCase = true) ||
        it.id.contains(searchQuery, ignoreCase = true)) &&
        // Filter by Class (if provided)
        (initialGrade == null || it.grade == initialGrade) &&
        (initialSection == null || it.section == initialSection)
    }.let { list ->
        when(sortOption) {
            SortOption.NAME_ASC -> list.sortedBy { "${it.lastName} ${it.firstName}" }
            SortOption.NAME_DESC -> list.sortedByDescending { "${it.lastName} ${it.firstName}" }
            SortOption.ID_ASC -> list.sortedBy { it.id }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = Color.White,
        sheetElevation = 16.dp,
        sheetContent = {
            AddEditStudentSheet(
                onDismiss = { scope.launch { sheetState.hide() } },
                viewModel = viewModel,
                studentToEdit = studentToEdit,
                fixedGrade = initialGrade,
                fixedSection = initialSection
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F7FA)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding() // Fix for status bar overlap
                    .padding(24.dp)
            ) {
                // Header / Top Bar
                if (isSelectionMode) {
                    TopAppBar(
                        title = { Text("${selectedStudents.size} selected", color = PrimaryBlue) },
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSelectionMode = false 
                                selectedStudents.clear()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = PrimaryBlue)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                // Batch Delete
                                selectedStudents.forEach { id -> viewModel.delete(id) }
                                isSelectionMode = false
                                selectedStudents.clear()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color.Red)
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // Back Button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                                }
                                Text(
                                    text = "Students Directory",
                                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                                )
                            }
                            Text(
                                text = if (initialGrade != null) "$initialGrade - $initialSection" else "${filteredStudents.size} registered",
                                style = MaterialTheme.typography.subtitle1.copy(color = Color.Gray),
                                modifier = Modifier.padding(start = 48.dp) // Align with title
                            )
                        }
                        
                        // Feature 3: Help Icon
                        IconButton(onClick = { showImportHelp = true }) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = Color.Gray)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                // Search and Sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search student...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = PrimaryBlue
                        ),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.width(8.dp))

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = PrimaryBlue)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(onClick = { sortOption = SortOption.NAME_ASC; showSortMenu = false }) {
                                Text("Name (A-Z)")
                            }
                            DropdownMenuItem(onClick = { sortOption = SortOption.NAME_DESC; showSortMenu = false }) {
                                Text("Name (Z-A)")
                            }
                            DropdownMenuItem(onClick = { sortOption = SortOption.ID_ASC; showSortMenu = false }) {
                                Text("ID")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Actions (Add/Import) - Hide during selection mode
                if (!isSelectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                studentToEdit = null
                                scope.launch { sheetState.show() }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Student", color = Color.White)
                        }
                        
                        Button(
                            onClick = { 
                                launcher.launch(arrayOf("text/*", "text/csv"))
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryTeal),
                            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(20.dp), tint = SecondaryTeal)
                            Spacer(Modifier.width(8.dp))
                            Text("Import CSV", color = SecondaryTeal)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // List
                if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No students found", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredStudents) { s ->
                            val isSelected = selectedStudents.contains(s.id)
                            
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = if (isSelected) 8.dp else 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (isSelectionMode) {
                                                if (isSelected) selectedStudents.remove(s.id) else selectedStudents.add(s.id)
                                                if (selectedStudents.isEmpty()) isSelectionMode = false
                                            } else {
                                                studentToEdit = s
                                                scope.launch { sheetState.show() }
                                            }
                                        },
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedStudents.add(s.id)
                                            }
                                        }
                                    ),
                                backgroundColor = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.White,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Selection Checkbox or Avatar
                                    if (isSelectionMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null, // Handled by card click
                                            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    } else {
                                        // Avatar Placeholder
                                        Surface(
                                            shape = androidx.compose.foundation.shape.CircleShape,
                                            color = PrimaryBlue.copy(alpha = 0.1f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                                            }
                                        }
                                        Spacer(Modifier.width(16.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${s.firstName} ${s.lastName}",
                                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${s.grade} - ${s.section} | ID: ${s.id}",
                                            style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                                        )
                                        // Display Default Credentials
                                        val displayUser = s.username ?: s.id
                                        val displayPass = s.password ?: "123456"
                                        Text(
                                            text = "User: $displayUser | Pass: $displayPass",
                                            style = MaterialTheme.typography.caption.copy(color = PrimaryBlue, fontWeight = FontWeight.Medium),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    if (!isSelectionMode) {
                                        // QR Button
                                        IconButton(onClick = { showQrForStudent = s }) {
                                            Icon(Icons.Default.QrCode, contentDescription = "Show QR", tint = PrimaryBlue)
                                        }
                                        
                                        // Quick Delete (Optional, or remove if preferring selection mode)
                                        IconButton(onClick = { viewModel.delete(s.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Feature 3: Import Help Dialog
            if (showImportHelp) {
                AlertDialog(
                    onDismissRequest = { showImportHelp = false },
                    title = { Text("Import Students via CSV") },
                    text = {
                        Column {
                            Text("You can import multiple students at once using a CSV file.")
                            Spacer(Modifier.height(8.dp))
                            Text("Option A (Auto-ID):", fontWeight = FontWeight.Bold)
                            Text("FirstName, LastName, Grade, Section", style = MaterialTheme.typography.caption)
                            Text("Example: Juan, Cruz, 10, A", color = Color.Gray, style = MaterialTheme.typography.caption)
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text("Option B (Custom ID):", fontWeight = FontWeight.Bold)
                            Text("ID, FirstName, LastName, Grade, Section", style = MaterialTheme.typography.caption)
                            Text("Example: S001, Juan, Cruz, 10, A", color = Color.Gray, style = MaterialTheme.typography.caption)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showImportHelp = false }) {
                            Text("Got it")
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // QR Dialog
            if (showQrForStudent != null) {
                val student = showQrForStudent!!
                Dialog(onDismissRequest = { showQrForStudent = null }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        elevation = 8.dp,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        backgroundColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${student.firstName} ${student.lastName}",
                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ID: ${student.id}",
                                style = MaterialTheme.typography.subtitle1.copy(color = Color.Gray)
                            )
                            
                            Spacer(Modifier.height(24.dp))
                            
                            // Generate QR
                            val payload = JSONObject().put("t", "student").put("i", student.id).toString()
                            val bitmap = QRGenerator.generate(payload)
                            
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.size(200.dp)
                                )
                            } else {
                                Text("Error generating QR")
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            // Feature 2: Print QR Button
                            Button(
                                onClick = { 
                                    if (bitmap != null) {
                                        PrintUtils.printBitmap(context, bitmap, "QR_${student.lastName}")
                                    }
                                },
                                shape = PillShape,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(backgroundColor = SecondaryTeal)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Print QR", color = Color.White)
                            }

                            Spacer(Modifier.height(12.dp))
                            
                            Button(
                                onClick = { showQrForStudent = null },
                                shape = PillShape,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class SortOption {
    NAME_ASC, NAME_DESC, ID_ASC
}
