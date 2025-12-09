package com.attendifyplus.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddEditStudentSheet(
    onDismiss: () -> Unit,
    viewModel: StudentListViewModel = getViewModel(),
    studentToEdit: StudentEntity? = null,
    fixedGrade: String? = null,
    fixedSection: String? = null
) {
    var firstName by remember(studentToEdit) { mutableStateOf(studentToEdit?.firstName ?: "") }
    var lastName by remember(studentToEdit) { mutableStateOf(studentToEdit?.lastName ?: "") }
    
    // Auto-generate ID if creating new student
    var id by remember(studentToEdit) { 
        mutableStateOf(studentToEdit?.id ?: viewModel.generateId()) 
    }
    
    // Use fixed values if provided, otherwise fallback to edit or empty
    var grade by remember(studentToEdit, fixedGrade) { 
        mutableStateOf(studentToEdit?.grade ?: fixedGrade ?: "") 
    }
    var section by remember(studentToEdit, fixedSection) { 
        mutableStateOf(studentToEdit?.section ?: fixedSection ?: "") 
    }

    // Bottom Sheet Content
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding() // Add padding to avoid overlapping with system navigation
            .imePadding(), // Add padding when keyboard opens
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle indicator
        Surface(
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .padding(bottom = 24.dp)
        ) {}

        Text(
            text = if (studentToEdit == null) "Add Student" else "Edit Student",
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface),
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("Student ID") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                textColor = MaterialTheme.colors.onSurface,
                leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                disabledTextColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                disabledLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            ),
            enabled = studentToEdit == null // Disable ID editing if editing existing student
        )
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    textColor = MaterialTheme.colors.onSurface,
                    leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    textColor = MaterialTheme.colors.onSurface
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text("Grade") },
                leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = fixedGrade == null,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    textColor = MaterialTheme.colors.onSurface,
                    leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            )
            OutlinedTextField(
                value = section,
                onValueChange = { section = it },
                label = { Text("Section") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = fixedSection == null,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    textColor = MaterialTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                viewModel.addOrUpdate(id, firstName, lastName, grade, section)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Save Student", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        // Bottom padding for navigation bars - increased to be safer
        Spacer(Modifier.height(100.dp))
    }
}

// Keeping old composable for backward compatibility if needed, but it now just wraps the sheet content logic if used as a full screen
@Composable
fun AddEditStudentScreen() {
   // Deprecated: Use AddEditStudentSheet inside a BottomSheetScaffold or ModalBottomSheetLayout
   // This is just a placeholder to prevent compilation errors if references exist
   Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
       AddEditStudentSheet(onDismiss = {})
   }
}
