package com.attendifyplus.ui.attendance

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
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
import com.attendifyplus.ui.theme.SuccessGreen
import org.koin.androidx.compose.getViewModel
import java.net.URLDecoder

@Composable
fun ConfirmScanScreen(
    navController: NavController, 
    scannedRaw: String?, 
    viewModel: AttendanceViewModel = getViewModel()
) {
    val context = LocalContext.current
    // Decode the payload since it comes from the URL
    val decodedPayload = remember(scannedRaw) {
        scannedRaw?.let { 
            try { URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { it }
        } ?: ""
    }
    
    var payload by remember { mutableStateOf(decodedPayload) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.QrCode, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp),
                        tint = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Confirm Attendance",
                style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Verify the student ID below",
                style = MaterialTheme.typography.subtitle1.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Data Card
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Student Identifier",
                        style = MaterialTheme.typography.caption.copy(color = PrimaryBlue)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = payload,
                        onValueChange = { payload = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = PillShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        viewModel.recordQr(payload)
                        Toast.makeText(context, "Attendance Confirmed", Toast.LENGTH_SHORT).show()
                        navController.popBackStack("dashboard/teacher", inclusive = false)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
