package com.attendifyplus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    var showNukeDialog by remember { mutableStateOf(false) }
    var nukePassword by remember { mutableStateOf("") }
    var nukeError by remember { mutableStateOf<String?>(null) }
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

    // Dialogs
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
            dismissButton = { TextButton(onClick = { showRemoteDialog = false }) { Text("Cancel") } }
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
            dismissButton = { TextButton(onClick = { showLocalDialog = false }) { Text("Cancel") } }
        )
    }

    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            title = { Text("Local Factory Reset") },
            text = { Text("This will wipe EVERYTHING LOCALLY: database, shared preferences, and session. It simulates a fresh install on this device. Server data remains intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.performFactoryReset()
                        showFactoryResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Wipe Local Only")
                }
            },
            dismissButton = { TextButton(onClick = { showFactoryResetDialog = false }) { Text("Cancel") } }
        )
    }
    
    if (showNukeDialog) {
        AlertDialog(
            onDismissRequest = { 
                showNukeDialog = false 
                nukePassword = ""
                nukeError = null
            },
            title = { Text("MASTER WIPE (NUKE ALL)") },
            text = { 
                Column {
                    Text("WARNING: This will delete ALL data from the SERVER (Firebase) AND this device. This essentially resets the entire project state for everyone.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Enter Admin Password to confirm:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nukePassword,
                        onValueChange = { 
                            nukePassword = it
                            nukeError = null
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = nukeError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (nukeError != null) {
                        Text(
                            text = nukeError!!,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nukePassword == "admin123") {
                            viewModel.nukeAllData()
                            showNukeDialog = false
                            nukePassword = ""
                            nukeError = null
                        } else {
                            nukeError = "Incorrect password"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("NUKE EVERYTHING", color = Color.White)
                }
            },
            dismissButton = { 
                TextButton(onClick = { 
                    showNukeDialog = false 
                    nukePassword = ""
                    nukeError = null
                }) { 
                    Text("Cancel") 
                } 
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )

            // 1. Remote Clear
            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Remote Database (Firebase)", style = MaterialTheme.typography.subtitle1)
                    Text("Delete records from the cloud only.", style = MaterialTheme.typography.body2, color = Color.Gray)
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

            // 2. Local Clear
            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Local Database (Device)", style = MaterialTheme.typography.subtitle1)
                    Text("Delete records on this device only.", style = MaterialTheme.typography.body2, color = Color.Gray)
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

            // 3. Factory Reset (Local)
            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xFFFFF3E0)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Local Factory Reset", style = MaterialTheme.typography.subtitle1)
                    Text("Wipe all local data & logout. Simulates fresh install.", style = MaterialTheme.typography.body2, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showFactoryResetDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE65100)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset App (Local Only)", color = Color.White)
                    }
                }
            }
            
            Divider()
            
            // 4. NUKE BUTTON
            Card(elevation = 8.dp, modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xFFFFEBEE)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dangerous, null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text("MASTER RESET", style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = Color.Red))
                    }
                    Text("Wipe Server AND Local data simultaneously. Use this to completely reset the project state for deployment.", style = MaterialTheme.typography.body2, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showNukeDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("NUKE EVERYTHING", color = Color.White, fontWeight = FontWeight.Bold)
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
