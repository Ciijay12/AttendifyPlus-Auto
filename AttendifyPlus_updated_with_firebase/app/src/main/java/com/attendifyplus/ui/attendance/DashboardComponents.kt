package com.attendifyplus.ui.attendance

import android.app.Activity
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.print.PrintHelper
import com.attendifyplus.R
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.RoyalIndigo
import com.attendifyplus.ui.theme.DeepPurple
import com.attendifyplus.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun InformationBoardCard(
    dailyStatus: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = PrimaryBlue,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Information Board",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = dailyStatus,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MyQrDialog(
    onDismiss: () -> Unit,
    studentName: String,
    studentId: String,
    qrCodeBitmap: Bitmap?,
    studentGradeSection: String // Added for more detail
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    DisposableEffect(Unit) {
        val window = activity?.window
        val originalBrightness = window?.attributes?.screenBrightness
        window?.attributes = window?.attributes?.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }

        onDispose {
            window?.attributes = window?.attributes?.apply {
                screenBrightness = originalBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.95f), // Increase dialog width
            backgroundColor = MaterialTheme.colors.surface // White background
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. App Logo or Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studentName.firstOrNull()?.toString() ?: "A",
                        style = MaterialTheme.typography.h4.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 2. Student Name
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )

                // 3. Student ID & Grade/Section
                Text(
                    text = "$studentId | $studentGradeSection",
                    style = MaterialTheme.typography.subtitle1.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
                )

                Spacer(Modifier.height(24.dp))

                // 4. QR Code with rounded corners and border
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 8.dp,
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
                ) {
                     qrCodeBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(220.dp) // Slightly larger QR
                        )
                    } ?: Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 5. Live Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .alpha(alpha)
                            .background(SuccessGreen, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.body2,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
                
                 Spacer(Modifier.height(8.dp))
                 
                Text(
                    text = "This code refreshes automatically.",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
