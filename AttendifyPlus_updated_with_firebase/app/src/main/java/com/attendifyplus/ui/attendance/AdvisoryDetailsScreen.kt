package com.attendifyplus.ui.attendance

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import org.koin.androidx.compose.getViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdvisoryDetailsDialog(
    onDismiss: () -> Unit,
    viewModel: AdvisoryDetailsViewModel = getViewModel()
) {
    val context = LocalContext.current
    val teacher by viewModel.teacher.collectAsState()
    val enabledTracks by viewModel.enabledTracks.collectAsState()
    
    // State derived from teacher but editable
    var grade by remember(teacher) { mutableStateOf(teacher?.advisoryGrade ?: "") }
    var section by remember(teacher) { mutableStateOf(teacher?.advisorySection ?: "") }
    var startTime by remember(teacher) { mutableStateOf(teacher?.advisoryStartTime ?: "") }
    
    // Support multiple strands: "ABM, STEM"
    var selectedTracks by remember(teacher) { 
        mutableStateOf(
            teacher?.advisoryTrack?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        ) 
    }

    // Check if class exists to show Delete button
    val isEditing = !teacher?.advisoryGrade.isNullOrBlank()
    val isShs = grade == "11" || grade == "12"
    
    // State for delete confirmation
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showTrackConfigDialog by remember { mutableStateOf(false) }
    var trackExpanded by remember { mutableStateOf(false) }

    // Gradient for Premium Look
    val premiumGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryBlue, SecondaryTeal)
    )
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Class?", color = MaterialTheme.colors.onSurface) },
            text = { Text("Are you sure you want to delete this class? This action cannot be undone.", color = MaterialTheme.colors.onSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClass()
                        showDeleteConfirmation = false
                        onDismiss()
                        Toast.makeText(context, "Class Deleted", Toast.LENGTH_SHORT).show()
                    }
                ) { 
                    Text("Delete", color = Color.Red) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { 
                    Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)) 
                }
            },
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    if (showTrackConfigDialog) {
        Dialog(onDismissRequest = { showTrackConfigDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp,
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Configure Tracks", style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue))
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val allPossibleTracks = listOf("ABM", "HUMSS", "STEM", "GAS", "AFA", "HE", "IA", "ICT", "Arts and Design", "Sports")
                        items(allPossibleTracks) { item ->
                            val isChecked = enabledTracks.contains(item)
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleTrack(item, !isChecked) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleTrack(item, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                )
                                Text(item, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showTrackConfigDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Advisory Class" else "Setup Advisory Class",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                    if (isShs) {
                        IconButton(onClick = { showTrackConfigDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configure Tracks", tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))

                // Inputs
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade Level") },
                    leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        backgroundColor = MaterialTheme.colors.surface,
                        cursorColor = PrimaryBlue,
                        textColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (isShs) {
                    ExposedDropdownMenuBox(
                        expanded = trackExpanded,
                        onExpandedChange = { trackExpanded = !trackExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedTracks.joinToString(", "), // Display comma-separated
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Tracks / Strands (Combine)") },
                            leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = PrimaryBlue) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trackExpanded) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                backgroundColor = MaterialTheme.colors.surface,
                                textColor = MaterialTheme.colors.onSurface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = trackExpanded,
                            onDismissRequest = { trackExpanded = false }
                        ) {
                            if (enabledTracks.isEmpty()) {
                                DropdownMenuItem(onClick = { showTrackConfigDialog = true }) {
                                    Text("Configure Tracks...", color = PrimaryBlue)
                                }
                            } else {
                                // Multi-select support
                                enabledTracks.sorted().forEach { option ->
                                    val isSelected = selectedTracks.contains(option)
                                    DropdownMenuItem(
                                        onClick = {
                                            val newSet = selectedTracks.toMutableSet()
                                            if (isSelected) newSet.remove(option) else newSet.add(option)
                                            selectedTracks = newSet
                                            // Don't close menu to allow multiple selections
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null, // Handled by parent click
                                                colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(option)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section Name") },
                    leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        backgroundColor = MaterialTheme.colors.surface,
                        cursorColor = PrimaryBlue,
                        textColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(16.dp))

                // Time Picker Field
                OutlinedTextField(
                    value = startTime.ifEmpty { "Not Set" },
                    onValueChange = { },
                    label = { Text("Class Start Time") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance()
                            val currentHour = if(startTime.isNotEmpty()) startTime.split(":")[0].toInt() else cal.get(Calendar.HOUR_OF_DAY)
                            val currentMinute = if(startTime.isNotEmpty()) startTime.split(":")[1].toInt() else cal.get(Calendar.MINUTE)

                            TimePickerDialog(context, { _, hour, minute ->
                                startTime = String.format("%02d:%02d", hour, minute)
                            }, currentHour, currentMinute, false).show()
                        },
                    enabled = false, // Make it read-only so click works on parent or needs a Box
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colors.onSurface,
                        disabledLabelColor = PrimaryBlue,
                        disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        disabledLeadingIconColor = PrimaryBlue,
                        backgroundColor = MaterialTheme.colors.surface
                    )
                )

                Spacer(Modifier.height(32.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditing) {
                        // Delete Button
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = PillShape,
                            border = BorderStroke(1.dp, Color.Red),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                             Icon(Icons.Default.Delete, contentDescription = null)
                             Spacer(Modifier.width(8.dp))
                             Text("Delete")
                        }
                    }

                    // Save Button
                    Button(
                        onClick = { 
                            if (grade.isNotBlank() && section.isNotBlank()) {
                                // Join selected tracks with comma
                                val finalTrack = if (isShs) selectedTracks.joinToString(", ") else null
                                viewModel.saveDetails(grade, section, startTime.ifBlank { null }, finalTrack)
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
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
                                text = "Save", 
                                color = Color.White, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
