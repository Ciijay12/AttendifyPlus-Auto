package com.attendifyplus.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal

@Composable
fun CustomTabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    // Determine colors based on selection and enabled state
    val contentColor = when {
        !enabled -> Color.Gray
        selected -> PrimaryBlue
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    }
    
    val backgroundColor = when {
        !enabled -> MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
        selected -> PrimaryBlue.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ScanTabItem(
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    // Distinct look for the Scan button
    val backgroundColor = if (!enabled) Color.LightGray else if (selected) SecondaryTeal else SecondaryTeal.copy(alpha = 0.1f)
    val iconColor = if (!enabled) Color.Gray else if (selected) Color.White else SecondaryTeal

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan",
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun UpdateCredentialsDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
                    text = "Update Credentials",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                Text(
                    text = "Set your new username and password. This can only be done once.",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("New Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password (Min 6 chars)") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                if (errorText != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorText!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.caption
                    )
                }
                
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
                            if (username.isBlank()) {
                                errorText = "Username cannot be empty"
                            } else if (password.length < 6) {
                                errorText = "Password must be at least 6 characters"
                            } else if (password != confirmPassword) {
                                errorText = "Passwords do not match"
                            } else {
                                onConfirm(username, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                        shape = PillShape
                    ) {
                        Text("Update", color = Color.White)
                    }
                }
            }
        }
    }
}
