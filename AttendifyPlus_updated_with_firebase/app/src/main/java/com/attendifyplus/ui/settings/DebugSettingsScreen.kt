package com.attendifyplus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@Composable
fun DebugSettingsScreen(
    navController: NavController,
    viewModel: DebugSettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRemoteDialog by remember { mutableStateOf(false) }
    var showLocalDialog by remember { mutableStateOf(false) }
    var showFactoryResetDialog by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is DebugUiState.Success -> {
                scaffoldState.snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is DebugUiState.Error -> {
                scaffoldState.snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (showRemoteDialog) {
        AlertDialog(
            onDismissRequest = { showRemoteDialog = false },
            title = { Text("Clear Remote Data") },
            text = { Text("Are you sure? This will delete ALL synced attendance, students, and config data from Firebase. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearRemoteData()
                        showRemoteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLocalDialog) {
        AlertDialog(
            onDismissRequest = { showLocalDialog = false },
            title = { Text("Clear Local Data") },
            text = { Text("Are you sure? This will wipe the database on this device. Synced data will be restored on next sync.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLocalData()
                        showLocalDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Wipe Device Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            title = { Text("Factory Reset (Fresh Install)") },
            text = { Text("Are you sure? This will wipe EVERYTHING: local database, shared preferences, and session data. It will be like a fresh app installation. You will need to login again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.performFactoryReset()
                        showFactoryResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Wipe Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Debug Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )

            Card(
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Remote Database (Firebase)", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Delete all records from the cloud. Use this to reset the project state.",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showRemoteDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Remote Data", color = Color.White)
                    }
                }
            }

            Card(
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Local Database (Device)", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Delete records on this device only.",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showLocalDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Local Data")
                    }
                }
            }

            Card(
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFFF3E0) // Light orange background for warning
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Factory Reset", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Wipe all local data, preferences, and session. Makes the app behave like a fresh install.",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showFactoryResetDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F)), // Darker red
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fresh Install Reset", color = Color.White)
                    }
                }
            }
            
            if (uiState is DebugUiState.Loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
