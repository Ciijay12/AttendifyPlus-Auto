package com.attendifyplus.ui.attendance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.utils.QrUtils
import kotlinx.coroutines.delay
import org.json.JSONObject

@Composable
fun MyQrDialog(onDismiss: () -> Unit) {
    // In a real app, we would get this ID from the logged-in User Session/Auth
    val studentId = "S001-DEMO" 
    val studentName = "Student User"
    
    // Anti-cheat: Auto-refresh timestamp
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Regenerate every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            timestamp = System.currentTimeMillis()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(Modifier.height(16.dp))

                Text(
                    text = studentName,
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Text(
                    text = "ID: $studentId",
                    style = MaterialTheme.typography.subtitle1.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
                
                Spacer(Modifier.height(32.dp))
                
                // Generate Dynamic QR
                val payload = JSONObject()
                    .put("t", "student")
                    .put("i", studentId)
                    .put("ts", timestamp) 
                    .toString()
                    
                val bitmap = remember(payload) { QrUtils.generateQr(payload, 600) }
                
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Dynamic QR Code",
                        modifier = Modifier.size(250.dp)
                    )
                } else {
                    Text("Error generating QR", color = MaterialTheme.colors.onSurface)
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Status indicator
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp), 
                            strokeWidth = 2.dp, 
                            color = PrimaryBlue
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Live Code",
                            style = MaterialTheme.typography.caption.copy(color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Show this to your teacher for attendance.",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
            }
        }
    }
}

@Composable
fun MyQrScreen(navController: NavController) {
    // Wrapper for existing usages if any, or redirects to dialog if preferred.
    // But for now, let's just keep it as a screen wrapper or show the dialog content directly.
    // Given the request to convert to popup, this screen might be deprecated or just wrap the dialog content in a full surface.
    // However, the user requested "MyQrScreen: Show the user's QR code in a Popup Dialog or Bottom Sheet from the Dashboard."
    // We've created MyQrDialog. The Dashboard will call it.
    
    // If someone navigates here directly, we show the content full screen (fallback)
    // or we can just show the same content.
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
        MyQrDialog(onDismiss = { navController.popBackStack() })
    }
}
