package com.attendifyplus.ui.attendance

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.local.entities.TeacherEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel

@Composable
fun TeacherCredentialsScreen(
    navController: NavController,
    viewModel: TeacherListViewModel = getViewModel()
) {
    val teachers by viewModel.teachers.collectAsState()
    val students by viewModel.students.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("All Grades") }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Teachers", "Students")

    // Reset Dialog State
    var showResetDialog by remember { mutableStateOf(false) }
    var teacherToReset by remember { mutableStateOf<TeacherEntity?>(null) }
    var studentToReset by remember { mutableStateOf<StudentEntity?>(null) }

    val filteredTeachers = remember(teachers, searchQuery, selectedGrade) {
        teachers
            .filter { teacher ->
                if (selectedGrade == "All Grades") true
                else teacher.advisoryGrade == selectedGrade
            }
            .filter { teacher ->
                if (searchQuery.isBlank()) true
                else teacher.firstName.contains(searchQuery, ignoreCase = true) || 
                     teacher.lastName.contains(searchQuery, ignoreCase = true) ||
                     teacher.username.contains(searchQuery, ignoreCase = true)
            }
    }
    
    val filteredStudents = remember(students, searchQuery, selectedGrade) {
        students
            .filter { student ->
                if (selectedGrade == "All Grades") true
                else student.grade == selectedGrade
            }
            .filter { student ->
                if (searchQuery.isBlank()) true
                else student.firstName.contains(searchQuery, ignoreCase = true) ||
                     student.lastName.contains(searchQuery, ignoreCase = true) ||
                     student.id.contains(searchQuery, ignoreCase = true) ||
                     (student.username?.contains(searchQuery, ignoreCase = true) == true)
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "User Credentials", // Renamed
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                    )
                    Text(
                        text = "Admin View Only",
                        style = MaterialTheme.typography.caption.copy(color = Color.Red)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = Color.Transparent,
                contentColor = PrimaryBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                        selectedContentColor = PrimaryBlue,
                        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            
            // Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search ${tabs[selectedTab]}") }, // Dynamic label
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )

                var expanded by remember { mutableStateOf(false) }
                val grades = listOf("All Grades") + (7..12).map { "Grade $it" }
                
                Box {
                    OutlinedTextField(
                        value = if (selectedGrade == "All Grades") "All Grades" else "Grade $selectedGrade",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter") },
                        modifier = Modifier.width(160.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = { 
                            Icon(
                                Icons.Default.ArrowDropDown, 
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            ) 
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onSurface,
                            leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            trailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            cursorColor = PrimaryBlue,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(160.dp)
                    ) {
                        grades.forEach { grade ->
                            DropdownMenuItem(onClick = { 
                                selectedGrade = grade.replace("Grade ", "")
                                expanded = false
                            }) {
                                Text(grade)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // List
            if (selectedTab == 0) {
                // Teachers List
                if (filteredTeachers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No teachers found", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredTeachers) { teacher ->
                            TeacherCredentialCard(
                                teacher = teacher,
                                onReset = {
                                    teacherToReset = teacher
                                    showResetDialog = true
                                },
                                onCopy = { u, p ->
                                    clipboardManager.setText(AnnotatedString("User: $u\nPass: $p"))
                                }
                            )
                        }
                    }
                }
            } else {
                // Students List
                if (filteredStudents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No students found", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredStudents) { student ->
                            StudentCredentialCard(
                                student = student,
                                onReset = {
                                    studentToReset = student
                                    showResetDialog = true
                                },
                                onCopy = { u, p ->
                                    clipboardManager.setText(AnnotatedString("User: $u\nPass: $p"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        if (teacherToReset != null) {
            ResetCredentialsDialog(
                name = "${teacherToReset!!.firstName} ${teacherToReset!!.lastName}",
                username = teacherToReset!!.username,
                onDismiss = { 
                    showResetDialog = false
                    teacherToReset = null
                },
                onConfirm = { u, p ->
                    viewModel.resetCredentials(teacherToReset!!.id, u, p)
                    showResetDialog = false
                    teacherToReset = null
                }
            )
        } else if (studentToReset != null) {
            val currentUsername = studentToReset!!.username ?: studentToReset!!.id
            ResetCredentialsDialog(
                name = "${studentToReset!!.firstName} ${studentToReset!!.lastName}",
                username = currentUsername,
                onDismiss = { 
                    showResetDialog = false
                    studentToReset = null
                },
                onConfirm = { u, p ->
                    viewModel.resetStudentCredentials(studentToReset!!.id, u, p)
                    showResetDialog = false
                    studentToReset = null
                }
            )
        }
    }
}

@Composable
fun TeacherCredentialCard(
    teacher: TeacherEntity,
    onReset: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${teacher.firstName} ${teacher.lastName}",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                IconButton(onClick = onReset, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Credentials", tint = Color.Red)
                }
            }
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f), MaterialTheme.shapes.small)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Username: ${teacher.username}", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Password: ", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                        if (passwordVisible) {
                            Text(teacher.password, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                        } else {
                            Text("••••••", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                        }
                    }
                    if (teacher.hasChangedCredentials) {
                        Text(
                            "Credential Changed", 
                            style = MaterialTheme.typography.caption.copy(color = Color.Green, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            "Default Credentials", 
                            style = MaterialTheme.typography.caption.copy(color = Color.Red, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { onCopy(teacher.username, teacher.password) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCredentialCard(
    student: StudentEntity,
    onReset: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val username = student.username ?: student.id
    val password = student.password ?: "123456" // Default if null

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${student.firstName} ${student.lastName}",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                    )
                    Text(
                        text = "${student.grade} - ${student.section}",
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                }
                
                IconButton(onClick = onReset, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Credentials", tint = Color.Red)
                }
            }
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f), MaterialTheme.shapes.small)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Username: $username", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Password: ", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                        if (passwordVisible) {
                            Text(password, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                        } else {
                            Text("••••••", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                        }
                    }
                    if (student.hasChangedCredentials) {
                        Text(
                            "Credential Changed", 
                            style = MaterialTheme.typography.caption.copy(color = Color.Green, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            "Default Credentials", 
                            style = MaterialTheme.typography.caption.copy(color = Color.Red, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { onCopy(username, password) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun ResetCredentialsDialog(
    name: String,
    username: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var newUsername by remember { mutableStateOf(username) }
    var newPassword by remember { mutableStateOf("123456") }
    var confirmPassword by remember { mutableStateOf("123456") }
    var errorText by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier.padding(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset Credentials",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = Color.Red)
                )
                Text(
                    text = "For: $name",
                    style = MaterialTheme.typography.subtitle2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("New Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    text = "This will also require the user to change password on next login.",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newUsername.isBlank()) {
                                errorText = "Username cannot be empty"
                            } else if (newPassword.length < 6) {
                                errorText = "Password must be at least 6 characters"
                            } else if (newPassword != confirmPassword) {
                                errorText = "Passwords do not match"
                            } else {
                                onConfirm(newUsername, newPassword)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        shape = PillShape
                    ) {
                        Text("Reset", color = Color.White)
                    }
                }
            }
        }
    }
}
