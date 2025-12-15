package com.attendifyplus.ui.attendance

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attendifyplus.ui.theme.DeepPurple
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.RoyalIndigo
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.WarningYellow
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(
    onLogin: (role: String, id: String) -> Unit,
    viewModel: LoginViewModel = getViewModel()
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    
    // State for handling selection: "none", "teacher", "student", "admin"
    var selectedRole by remember { mutableStateOf<String?>(null) }
    
    // Dialog State
    var showLoginDialog by remember { mutableStateOf(false) }
    
    // First-Time Help Dialog
    var showHelpDialog by remember { mutableStateOf(false) }

    // Force Change Credentials State
    var showForceChangeDialog by remember { mutableStateOf(false) }
    var firstTimeRole by remember { mutableStateOf("") }
    var firstTimeId by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                onLogin(state.role, state.id)
                viewModel.resetState()
                showLoginDialog = false
                showForceChangeDialog = false // Dismiss if success after update
                selectedRole = null
            }
            is LoginState.FirstTimeLogin -> {
                showLoginDialog = false // Close normal login
                firstTimeRole = state.role
                firstTimeId = state.id
                showForceChangeDialog = true
            }
            is LoginState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Add Wavy Header background for Login Screen
            WavyHeaderLogin()

            // Centered Content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Spacer(Modifier.height(100.dp)) // Push content down below the wave

                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White,
                    elevation = 8.dp,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "App Logo",
                            modifier = Modifier.size(60.dp),
                            tint = PrimaryBlue
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "AttendifyPlus",
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Help Text Button
                TextButton(onClick = { showHelpDialog = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colors.onBackground.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                         Spacer(Modifier.width(4.dp))
                         Text("First time here?", color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f))
                    }
                }
                
                Spacer(Modifier.height(32.dp))

                // 2 Main Cards: Teacher & Student
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Teacher Card
                    LoginRoleCard(
                        title = "Teacher",
                        icon = Icons.Default.Person,
                        color = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedRole = "teacher"
                            showLoginDialog = true 
                        }
                    )

                    // Student Card
                    LoginRoleCard(
                        title = "Student",
                        icon = Icons.Default.School,
                        color = SecondaryTeal,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedRole = "student"
                            showLoginDialog = true
                        }
                    )
                }
            }
            
            // Admin Access - Bottom
            TextButton(
                onClick = { 
                     selectedRole = "admin"
                     showLoginDialog = true 
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Admin Access", 
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.button
                )
            }

            // Login Dialog
            if (showLoginDialog && selectedRole != null) {
                LoginDialog(
                    role = selectedRole!!,
                    onDismiss = { showLoginDialog = false },
                    onLoginAction = { user, pass ->
                        when(selectedRole) {
                            "teacher", "admin" -> viewModel.loginTeacher(user, pass)
                            "student" -> viewModel.loginStudent(user, pass) // Updated to pass both
                        }
                    },
                    isLoading = loginState is LoginState.Loading
                )
            }

            // Non-Dismissible First Time Login Dialog
            if (showForceChangeDialog) {
                ForceChangeCredentialsDialog(
                    role = firstTimeRole,
                    onUpdate = { newUser, newPass ->
                        viewModel.updateCredentials(firstTimeRole, firstTimeId, newUser, newPass)
                    },
                    isLoading = loginState is LoginState.Loading
                )
            }
            
            // First Time Help Dialog
            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = { Text("Welcome to AttendifyPlus", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface) },
                    text = {
                        Column {
                            Text("New User Instructions:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Text("• Teachers: Please contact the Administrator to register your account and get your login credentials.", color = MaterialTheme.colors.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Text("• Students: Please contact your Adviser or Subject Teacher to enroll you in their class. Your Student ID is your default username.", color = MaterialTheme.colors.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Text("• Default Admin Credentials: Username 'admin', Password 'admin123'. (This is hardcoded for demonstration).", color = MaterialTheme.colors.onSurface)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Got it", color = PrimaryBlue)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colors.surface
                )
            }
        }
    }
}

@Composable
fun WavyHeaderLogin() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp) 
    ) {
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
        
        // Text Content with Dynamic Status Bar Padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() 
                .padding(top = 16.dp, start = 24.dp, end = 24.dp), 
            verticalArrangement = Arrangement.Top 
        ) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Main Title
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Ipil National High School",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Attendance Monitoring System",
                        style = MaterialTheme.typography.body2.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }

                // Pill Badge
                Surface(
                    shape = PillShape,
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Research Project",
                        style = MaterialTheme.typography.caption.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp 
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginRoleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = modifier
            .height(150.dp)
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface),
            )
        }
    }
}

@Composable
fun LoginDialog(
    role: String,
    onDismiss: () -> Unit,
    onLoginAction: (String, String) -> Unit,
    isLoading: Boolean
) {
    var field1 by remember { mutableStateOf("") } // Username or Student ID
    var field2 by remember { mutableStateOf("") } // Password
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${role.replaceFirstChar { it.uppercase() }} Login",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                
                Spacer(Modifier.height(24.dp))
                
                // First Field
                // For Student: Student ID or Username
                // For Teacher: Username
                OutlinedTextField(
                    value = field1,
                    onValueChange = { field1 = it },
                    label = { Text(if (role == "student") "Student ID / Username" else "Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Second Field - Always Password now
                OutlinedTextField(
                    value = field2,
                    onValueChange = { field2 = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
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
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )

                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { 
                            onLoginAction(field1, field2) 
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ForceChangeCredentialsDialog(
    role: String,
    onUpdate: (String, String) -> Unit,
    isLoading: Boolean
) {
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {}, // Non-dismissible
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = WarningYellow.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = "Reset",
                            tint = WarningYellow,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Update Credentials",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                
                Text(
                    text = "For your security, you must update your default credentials before proceeding.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(Modifier.height(24.dp))

                // New Username Field
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("New Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryBlue) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryBlue) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryBlue) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = error!!, color = Color.Red, style = MaterialTheme.typography.caption)
                }

                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (newUsername.isBlank() || newPassword.isBlank()) {
                                error = "All fields are required"
                            } else if (newPassword != confirmPassword) {
                                error = "Passwords do not match"
                            } else if (newPassword.length < 6) {
                                error = "Password must be at least 6 characters"
                            } else {
                                error = null
                                onUpdate(newUsername, newPassword)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
                    ) {
                        Text("Update & Login", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
