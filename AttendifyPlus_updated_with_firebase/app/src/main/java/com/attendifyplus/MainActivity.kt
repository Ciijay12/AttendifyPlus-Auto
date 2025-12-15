package com.attendifyplus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.attendifyplus.sync.SyncWorker
import com.attendifyplus.ui.attendance.NavHostContainer
import com.attendifyplus.ui.theme.AttendifyTheme
import timber.log.Timber
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("Notification permission granted")
        } else {
            Timber.w("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        // We set this to true initially to keep the system splash visible
        // However, since we made the system splash "invisible" (transparent icon on white bg),
        // we want to dismiss it AS FAST AS POSSIBLE so our Custom Compose Splash takes over.
        var isSystemSplashVisible = true
        splashScreen.setKeepOnScreenCondition { isSystemSplashVisible }

        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        askNotificationPermission()
        
        // Robust Sync on App Entry
        triggerForcedSync("app_entry_sync")

        setContent {
            AttendifyTheme {
                var showCustomSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    // Immediately dismiss the system splash screen
                    // This allows our CustomSplashScreen to render instantly.
                    isSystemSplashVisible = false
                    
                    // Show our custom splash for 2.5 seconds
                    delay(2500)
                    showCustomSplash = false
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!showCustomSplash) {
                        NavHostContainer()
                    }

                    AnimatedVisibility(
                        visible = showCustomSplash,
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        CustomSplashScreen()
                    }
                }
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        // Robust Sync on App Exit / Background
        triggerForcedSync("app_exit_sync")
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Timber.i("Requesting notification permission")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    private fun triggerForcedSync(tag: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(tag)
            .build()

        // Use Unique Work with KEEP to ensure we don't spam if already running, 
        // but ensure at least one runs when entering/exiting.
        // For exiting, we might want REPLACE to ensure it really tries, but KEEP is safer for data integrity.
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "global_app_sync",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
        Timber.d("Triggered forced sync: $tag")
    }
}

@Composable
fun CustomSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Logo - Use the original PNG resource.
            // Compose's Image composable with ContentScale.Fit ensures the entire logo is visible 
            // without being cropped, regardless of the container size.
            Image(
                painter = painterResource(id = R.drawable.logo_playstore),
                contentDescription = "Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(150.dp) // Adjusted size for better visibility
                    .padding(16.dp) // Add padding to give it some breathing room
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "AttendifyPlus",
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            )
        }

        // Footer Section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Research Project",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Version " + BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.caption.copy(
                    color = Color.LightGray
                )
            )
        }
    }
}
