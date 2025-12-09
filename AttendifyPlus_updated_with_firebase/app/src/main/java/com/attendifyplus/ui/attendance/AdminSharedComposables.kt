package com.attendifyplus.ui.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal

@Composable
fun GradeCard(
    grade: String,
    studentCount: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.3f) // Square-ish aspect ratio
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = grade,
                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
            }
            
            Column {
                Text(
                    text = "Grade $grade",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Text(
                    text = "$studentCount Students",
                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
            }
        }
    }
}

@Composable
fun StatChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        backgroundColor = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
fun DepartmentTab(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(if (selected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
    val contentColor by animateColorAsState(if (selected) PrimaryBlue else MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = contentColor, fontWeight = fontWeight, style = MaterialTheme.typography.button, textAlign = TextAlign.Center)
    }
}

@Composable
fun ImportHelpDialog(
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = 12.dp,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Import Instructions",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = "Please ensure your CSV file follows this format:",
                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                
                Surface(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "FirstName, LastName, Grade, Section",
                        style = MaterialTheme.typography.caption.copy(
                            fontFamily = FontFamily.Monospace, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        ),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = "• Student IDs will be auto-generated.\n• The first row (header) will be skipped.",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = PillShape,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent)
                    ) {
                        Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onProceed,
                        shape = PillShape,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Select File", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentListItemPremium(
    student: StudentEntity,
    onEdit: (StudentEntity) -> Unit,
    onDelete: (StudentEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f)),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryBlue,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = student.firstName.take(1).uppercase(),
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.lastName}, ${student.firstName}",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SecondaryTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Grade ${student.grade} - ${student.section}",
                            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = SecondaryTeal),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "ID: ${student.id}",
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                    )
                }
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onEdit(student)
                    }) {
                        Icon(Icons.Default.Edit, null, tint = PrimaryBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit", color = MaterialTheme.colors.onSurface)
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onDelete(student)
                    }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text("Archive/Delete", color = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddStudentPremiumDialog(
    advisoryClasses: List<AdvisoryClassOption>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, AdvisoryClassOption) -> Unit,
    onGenerateId: () -> String,
    initialStudent: StudentEntity? = null,
    forcedAdvisoryClass: AdvisoryClassOption? = null
) {
    var firstName by remember { mutableStateOf(initialStudent?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialStudent?.lastName ?: "") }
    var studentId by remember { mutableStateOf(initialStudent?.id ?: onGenerateId()) }
    
    // Pre-select advisory class logic
    var selectedAdvisory by remember { 
        mutableStateOf(
            if (forcedAdvisoryClass != null) forcedAdvisoryClass
            else if (initialStudent != null) {
                advisoryClasses.find { it.grade == initialStudent.grade && it.section == initialStudent.section }
            } else if (advisoryClasses.size == 1) {
                advisoryClasses.first()
            } else null
        ) 
    }
    var expanded by remember { mutableStateOf(false) }

    val isSetupRequired = advisoryClasses.isEmpty() && forcedAdvisoryClass == null
    val isEditing = initialStudent != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = 12.dp,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSetupRequired) "Setup Required" else if (isEditing) "Edit Student" else "Add New Student",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                
                Spacer(Modifier.height(24.dp))

                if (isSetupRequired) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800), // Orange warning
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No advisory classes found.",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "You must assign 'Advisory Classes' to teachers before adding students.",
                        style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Close", color = Color.White)
                    }

                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Advisory Class (Required)",
                            style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    if (forcedAdvisoryClass != null || advisoryClasses.size == 1) {
                        // Read-only Text Field for single/forced class
                        OutlinedTextField(
                            value = selectedAdvisory?.toString() ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false, // Visual cue
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colors.onSurface,
                                disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                                backgroundColor = MaterialTheme.colors.surface
                            )
                        )
                    } else {
                        // Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedAdvisory?.toString() ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select Class", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                                    backgroundColor = MaterialTheme.colors.surface,
                                    textColor = MaterialTheme.colors.onSurface
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                advisoryClasses.forEach { option ->
                                    DropdownMenuItem(onClick = {
                                        selectedAdvisory = option
                                        expanded = false
                                    }) {
                                        Text(option.toString())
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    PremiumInput(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = "Student ID",
                        icon = Icons.Default.School
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    PremiumInput(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First Name",
                        icon = Icons.Default.Person
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    PremiumInput(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last Name",
                        icon = Icons.Default.Person
                    )

                    Spacer(Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = PillShape,
                            border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent)
                        ) {
                            Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (studentId.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank() && selectedAdvisory != null) {
                                    onSave(studentId, firstName, lastName, selectedAdvisory!!)
                                }
                            },
                            enabled = studentId.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank() && selectedAdvisory != null,
                            shape = PillShape,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = PrimaryBlue,
                                disabledBackgroundColor = Color.Gray
                            )
                        ) {
                            Text(if (isEditing) "Update" else "Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(icon, null, tint = PrimaryBlue) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                backgroundColor = MaterialTheme.colors.surface,
                textColor = MaterialTheme.colors.onSurface
            )
        )
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    isSmall: Boolean = false
) {
    val backgroundColor = if (selected) PrimaryBlue else MaterialTheme.colors.surface
    val contentColor = if (selected) Color.White else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
    val borderColor = if (selected) PrimaryBlue else MaterialTheme.colors.onSurface.copy(alpha = 0.2f)

    Surface(
        shape = PillShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = contentColor,
            style = if (isSmall) MaterialTheme.typography.caption else MaterialTheme.typography.body2,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = if(isSmall) 12.dp else 16.dp, vertical = if(isSmall) 6.dp else 8.dp)
        )
    }
}
