package com.attendifyplus.ui.attendance

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RetroactiveAttendanceScreen(
    navController: NavController,
    viewModel: RetroactiveAttendanceViewModel = getViewModel()
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val saveMessage by viewModel.saveState.collectAsState()

    // Date State
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Selection State
    val selectedStudentIds = remember { mutableStateListOf<String>() }
    
    // Filter/Search
    var searchQuery by remember { mutableStateOf("") }
    
    // Status to apply
    var statusToApply by remember { mutableStateOf("present") }

    // Select All Logic
    val areAllSelected = students.isNotEmpty() && selectedStudentIds.size == students.size
    
    // Date Picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val newDate = Calendar.getInstance()
            newDate.set(year, month, dayOfMonth)
            selectedDate = newDate
        },
        selectedDate.get(Calendar.YEAR),
        selectedDate.get(Calendar.MONTH),
        selectedDate.get(Calendar.DAY_OF_MONTH)
    )
    // Failproof: Prevent selecting future dates
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Filter students logic
    val filteredStudents = students.filter {
        it.firstName.contains(searchQuery, ignoreCase = true) || 
        it.lastName.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.startsWith("Success")) {
                navController.popBackStack()
            }
            viewModel.clearMessage()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Added padding for status bar
                .padding(24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onBackground)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Record Skipped Attendance",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Date Selector
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = 2.dp,
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Attendance Date", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                        Text(
                            text = dateFormat.format(selectedDate.time),
                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                        )
                    }
                    Icon(Icons.Default.EditCalendar, contentDescription = null, tint = PrimaryBlue)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Controls: Search & Select All
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search student...", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = MaterialTheme.colors.surface,
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                    )
                )
                Spacer(Modifier.width(12.dp))
                
                // Select All Checkbox
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Checkbox(
                        checked = areAllSelected,
                        onCheckedChange = { checked ->
                            selectedStudentIds.clear()
                            if (checked) {
                                selectedStudentIds.addAll(students.map { it.id })
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue, uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    )
                    Text("All", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface)
                }
            }

            Spacer(Modifier.height(8.dp))
            
            Text(
                "${selectedStudentIds.size} students selected", 
                style = MaterialTheme.typography.caption, 
                color = PrimaryBlue
            )

            Spacer(Modifier.height(8.dp))

            // Student List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents) { student ->
                    val isSelected = selectedStudentIds.contains(student.id)
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = 1.dp, // Reduced elevation/shadow to be minimal
                        backgroundColor = MaterialTheme.colors.surface, // Always surface color
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (isSelected) selectedStudentIds.remove(student.id)
                            else selectedStudentIds.add(student.id)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null, // Handled by card click
                                colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue, uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${student.lastName}, ${student.firstName}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colors.onSurface)
                                Text("ID: ${student.id}", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bottom Action Bar
            Card(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mark selected as:", style = MaterialTheme.typography.subtitle2, color = MaterialTheme.colors.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("present", "late", "absent").forEach { opt ->
                            val isStatusSelected = statusToApply == opt
                            val color = when(opt) {
                                "present" -> PrimaryBlue
                                "late" -> Color(0xFFFFC107)
                                "absent" -> Color.Red
                                else -> Color.Gray
                            }
                            
                            Button(
                                onClick = { statusToApply = opt },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isStatusSelected) color else MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                                    contentColor = if (isStatusSelected) Color.White else MaterialTheme.colors.onSurface
                                ),
                                shape = PillShape,
                                elevation = ButtonDefaults.elevation(0.dp),
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            ) {
                                Text(opt.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                             if (selectedStudentIds.isEmpty()) {
                                 Toast.makeText(context, "No students selected", Toast.LENGTH_SHORT).show()
                             } else {
                                 viewModel.saveAttendance(
                                     dateTimestamp = selectedDate.timeInMillis,
                                     selectedStudentIds = selectedStudentIds.toList(),
                                     status = statusToApply
                                 )
                             }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = SecondaryTeal)
                    ) {
                        Text("SAVE ATTENDANCE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
