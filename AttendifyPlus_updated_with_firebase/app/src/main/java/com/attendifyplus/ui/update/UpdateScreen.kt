package com.attendifyplus.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attendifyplus.BuildConfig

@Composable
fun UpdateScreen(updateViewModel: UpdateViewModel = viewModel()) {
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdate()
    }

    updateInfo?.let {
        if (it.versionCode > BuildConfig.VERSION_CODE) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Update Required") },
                text = {
                    Column {
                        Text("A new version of AttendifyPlus is available and required to continue.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Version: ${it.versionName}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it.releaseNotes)
                    }
                },
                confirmButton = {
                    Button(onClick = { downloadUpdate(context, it.downloadUrl) }) {
                        Text("Download Update")
                    }
                },
                dismissButton = null // Compulsory update
            )
        }
    }
}

private fun downloadUpdate(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }
    context.startActivity(intent)
}
