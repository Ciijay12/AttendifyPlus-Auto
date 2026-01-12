package com.attendifyplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attendifyplus.ui.attendance.DashboardViewModel
import com.attendifyplus.ui.theme.DeepPurple
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.RoyalIndigo
import org.koin.androidx.compose.getViewModel

@Composable
fun GlobalUpdateHandler(
    viewModel: DashboardViewModel = getViewModel()
) {
    val updateConfig by viewModel.updateConfig.collectAsState()

    if (updateConfig != null) {
        val config = updateConfig!!
        
        // Forced Dialog: Cannot be dismissed
        Dialog(
            onDismissRequest = { /* Do nothing to prevent dismissal */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Gradient with Icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(RoyalIndigo, DeepPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mandatory Update Required",
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colors.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.1f),
                            shape = PillShape
                        ) {
                            Text(
                                text = "v${config.versionName}",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Consequence Warning Section
                        Card(
                            backgroundColor = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            elevation = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Warning",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Failure to update will result in synchronization errors and restricted access to attendance features.",
                                    style = MaterialTheme.typography.caption.copy(
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = config.releaseNotes.ifBlank { "Critical security and synchronization improvements." },
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Button (Only Update Now)
                        Button(
                            onClick = { viewModel.downloadUpdate(config.downloadUrl) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                            elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "UPDATE NOW",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Access will be restored after installation.",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
