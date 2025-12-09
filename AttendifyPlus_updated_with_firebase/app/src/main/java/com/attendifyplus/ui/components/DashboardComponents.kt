package com.attendifyplus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.attendifyplus.ui.theme.DeepPurple
import com.attendifyplus.ui.theme.Dimens
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.RoyalIndigo
import com.attendifyplus.ui.theme.SuccessGreen
import com.attendifyplus.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Unified WavyHeader for all screens (Login, Dashboard, Inner Screens)
@Composable
fun WavyHeaderPremium(
    title: String,
    subtitle: String = "",
    userName: String = "",
    onProfileClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.HeaderHeight) 
    ) {
        // Background Wave
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, size.height * 0.75f)
                cubicTo(
                    size.width * 0.25f, size.height,
                    size.width * 0.75f, size.height * 0.5f,
                    size.width, size.height * 0.85f
                )
                lineTo(size.width, 0f)
                close()
            }
            
            // Draw Shadow
            translate(left = 0f, top = 15f) {
                drawPath(
                    path = path,
                    color = Color.Black.copy(alpha = 0.2f)
                )
            }

            // Draw Gradient
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(RoyalIndigo, DeepPurple)
                )
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Dimens.PaddingLarge, vertical = 12.dp) 
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                     Text(
                        text = if (userName.isNotEmpty()) "AttendifyPlus" else title,
                        style = MaterialTheme.typography.h5.copy( 
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.25f),
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Spacer(Modifier.height(Dimens.PaddingTiny)) 
                    
                    val subText = if (userName.isNotEmpty()) "Hello, $userName" else subtitle
                    if (subText.isNotEmpty()) {
                        Text(
                            text = subText,
                            style = MaterialTheme.typography.subtitle1.copy( 
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
                
                if (onProfileClick != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(Dimens.IconSizeLarge) 
                    ) {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Keeping original for backward compatibility if needed, but delegating
@Composable
fun WavyHeader(userName: String, onProfileClick: () -> Unit = {}) {
    WavyHeaderPremium(
        title = "AttendifyPlus",
        userName = userName,
        onProfileClick = onProfileClick
    )
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = Dimens.CardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.DashboardCardHeight)
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(Dimens.IconSizeExtraLarge)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            Text(
                text = title,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun SummaryCard(
    present: Int,
    late: Int,
    absent: Int
) {
    val currentDate = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    Card(
        shape = RoundedCornerShape(Dimens.CornerRadiusExtraLarge),
        elevation = Dimens.CardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.SummaryCardHeight),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Summary",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.caption.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Present", count = present, color = SuccessGreen)
                StatItem(label = "Late", count = late, color = WarningYellow)
                StatItem(label = "Absent", count = absent, color = Color.Red)
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold
            )
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(Dimens.CornerRadiusLarge),
            elevation = 8.dp,
            modifier = Modifier.padding(Dimens.PaddingMedium),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(Dimens.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Update Credentials",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                Text(
                    text = "Please set your new username and password. This can only be done once.",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
                )
                
                Spacer(Modifier.height(Dimens.PaddingMedium))
                
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
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
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
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                if (errorMessage != null) {
                    Spacer(Modifier.height(Dimens.PaddingSmall))
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.caption
                    )
                }
                
                Spacer(Modifier.height(Dimens.PaddingLarge))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.width(Dimens.PaddingSmall))
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "All fields are required"
                            } else if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                            } else {
                                onConfirm(username, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Update", color = Color.White)
                    }
                }
            }
        }
    }
}
