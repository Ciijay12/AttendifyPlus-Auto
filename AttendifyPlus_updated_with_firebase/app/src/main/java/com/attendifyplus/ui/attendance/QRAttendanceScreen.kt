package com.attendifyplus.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* 
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.WarningYellow
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.*
import org.json.JSONObject
import org.koin.androidx.compose.getViewModel
import java.util.concurrent.Executors
import kotlin.math.abs

@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun QRAttendanceScreen(
    navController: NavController,
    subjectName: String? = null,
    viewModel: AttendanceViewModel = getViewModel(),
    onBack: () -> Unit = { navController.popBackStack() }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    // Camera Permission State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Scan State
    val scanState by viewModel.scanState.collectAsState()
    val subjectClasses by viewModel.subjectClasses.collectAsState()

    // UI state for manual mode toggle
    var isManualMode by remember { mutableStateOf(false) }
    
    // UI state for last scanned value + cooldown
    var lastScanTimestamp by remember { mutableStateOf(0L) }
    val cooldownMs = 1500L
    val validQrWindowMs = 60000L // 60 seconds

    // Torch state
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    // Success/Error Feedback State for Overlay
    var feedbackState by remember { mutableStateOf<ScanFeedback?>(null) }

    // Selected Subject State
    var selectedSubject by remember { mutableStateOf<String?>(subjectName) }

    // BottomSheet Modal State for Subject Selection
    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    
    // Auto-show Bottom Sheet if no subject is selected
    LaunchedEffect(subjectName, selectedSubject) {
        if (subjectName == null && selectedSubject == null) {
            modalSheetState.show()
        }
    }

    // Back Handler: If sheet is open, close it. If sheet is closed, go back.
    BackHandler(enabled = true) {
        scope.launch {
            if (modalSheetState.isVisible) {
                modalSheetState.hide()
            } else {
                onBack()
            }
        }
    }

    // Executor for camera analysis (disposed properly)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(scanState) {
        if (scanState is ScanState.Success) {
            val status = (scanState as ScanState.Success).status
            feedbackState = ScanFeedback(
                isSuccess = true,
                message = if (status == "late") "Marked Late" else "Attendance Recorded",
                studentId = (scanState as ScanState.Success).studentId
            )
            delay(2000)
            feedbackState = null
            viewModel.resetScanState()
        } else if (scanState is ScanState.Error) {
            feedbackState = ScanFeedback(
                isSuccess = false,
                message = (scanState as ScanState.Error).message
            )
            delay(2000)
            feedbackState = null
            viewModel.resetScanState()
        }
    }

    // Modal Bottom Sheet Layout
    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = MaterialTheme.colors.surface,
        sheetContent = {
            // Added Box with padding for consistency
            Box(modifier = Modifier.navigationBarsPadding()) {
                SubjectSelectionSheet(
                    subjects = subjectClasses,
                    onSubjectSelected = { selected ->
                        selectedSubject = selected.subjectName
                        scope.launch { modalSheetState.hide() }
                    }
                )
            }
        }
    ) {
        // Main Scanner Content
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            
            // 1. Camera Preview (Bottom Layer)
            if (!isManualMode) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build()
                                preview.setSurfaceProvider(previewView.surfaceProvider)

                                val options = BarcodeScannerOptions.Builder()
                                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                    .build()
                                val scanner = BarcodeScanning.getClient(options)

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    processImageProxy(scanner, imageProxy) { rawValue ->
                                        val now = System.currentTimeMillis()
                                        if (now - lastScanTimestamp > cooldownMs && rawValue != null && scanState is ScanState.Idle) {
                                            lastScanTimestamp = now
                                            
                                            // Basic validation
                                            var parsedId: String? = rawValue
                                            var isValid = true
                                            
                                            try {
                                                val json = JSONObject(rawValue)
                                                if (json.has("i")) {
                                                    parsedId = json.getString("i")
                                                    if (json.has("ts")) {
                                                        val ts = json.getLong("ts")
                                                        if (abs(now - ts) > validQrWindowMs) {
                                                            isValid = false
                                                        }
                                                    }
                                                }
                                            } catch (_: Exception) {
                                                parsedId = rawValue
                                            }
                                            
                                            if (isValid && parsedId != null) {
                                                // Only proceed if a subject is selected
                                                val finalSubject = selectedSubject ?: subjectName
                                                
                                                if (finalSubject != null) {
                                                     viewModel.recordQr(parsedId, "subject", finalSubject)
                                                } else {
                                                    // If modal is hidden but no subject, re-show modal
                                                    scope.launch { modalSheetState.show() }
                                                }
                                            }
                                        }
                                    }
                                }

                                try {
                                    cameraProvider.unbindAll()
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraControl = camera.cameraControl
                                } catch (e: Exception) {
                                    Log.e("QRAttendanceScreen", "Camera bind failed", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Camera permission required", color = Color.White)
                    }
                }

                // 2. Dark Overlay with Cutout & Animation
                ModernScannerOverlay(feedbackState = feedbackState)
            } else {
                // Manual Mode Background
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background))
            }

            // 3. Top Controls (Back, Title, Torch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                // Mode Switcher (Pill)
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        ModeButton(
                            text = "Scan",
                            isSelected = !isManualMode,
                            onClick = { isManualMode = false }
                        )
                        Spacer(Modifier.width(4.dp))
                        ModeButton(
                            text = "Manual",
                            isSelected = isManualMode,
                            onClick = { isManualMode = true }
                        )
                    }
                }

                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isTorchOn) Color.White else Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.FlashOn,
                        contentDescription = "Flash",
                        tint = if (isTorchOn) Color.Black else Color.White
                    )
                }
            }
            
            // 4. Main Content Area (Manual Input or Feedback)
            Box(
                modifier = Modifier
                    .fillMaxSize(), // Use fillMaxSize to center content properly
                contentAlignment = Alignment.Center // Center alignment
            ) {
                if (isManualMode) {
                    // Full Screen Manual Entry
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 120.dp) // Added 120dp top padding
                            .imePadding() // Handle keyboard
                    ) {
                        ManualEntryCard(
                            subjectName = selectedSubject ?: subjectName,
                            viewModel = viewModel,
                            onSelectSubject = { scope.launch { modalSheetState.show() } }
                        )
                    }
                } else {
                    // Bottom Tip or Context Indicator - Positioned at bottom center
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                    ) {
                        AnimatedVisibility(
                            visible = feedbackState == null,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                
                                // Show Current Subject or Prompt
                                val displaySubject = selectedSubject ?: subjectName
                                
                                if (displaySubject != null) {
                                     Text(
                                        text = "Subject: $displaySubject",
                                        color = SecondaryTeal,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                            .clickable { scope.launch { modalSheetState.show() } }
                                    )
                                } else {
                                     Text(
                                        text = "Select Subject Class",
                                        color = WarningYellow,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                            .clickable { scope.launch { modalSheetState.show() } }
                                    )
                                }
                                
                                Text(
                                    text = "Point camera at student QR code",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectSelectionSheet(
    subjects: List<SubjectClassEntity>,
    onSubjectSelected: (SubjectClassEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .heightIn(max = 400.dp) // Limit height
    ) {
        Text(
            text = "Select Subject",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )
        Text(
            text = "Choose a class to take attendance for",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        
        if (subjects.isEmpty()) {
             Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("No subjects found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(subjects) { subject ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSubjectSelected(subject) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = 0.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f)),
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subject.subjectName.firstOrNull()?.toString() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(subject.subjectName, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                                Text(
                                    "${subject.gradeLevel} - ${subject.section}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    // Dynamic colors for selected/unselected state
    val backgroundColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) PrimaryBlue else Color.White // Use PrimaryBlue for selected text to pop

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold, // Make bold for better visibility
            fontSize = 14.sp
        )
    }
}

@Composable
fun ModernScannerOverlay(
    feedbackState: ScanFeedback?
) {
    val infiniteTransition = rememberInfiniteTransition()
    val anim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart)
    )
    
    val primaryColor = when {
        feedbackState?.isSuccess == true -> SecondaryTeal
        feedbackState?.isSuccess == false -> Color.Red
        else -> PrimaryBlue
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val scanSize = w * 0.7f
            val left = (w - scanSize) / 2
            val top = (h - scanSize) / 2 - (h * 0.1f) // Slightly above center
            val right = left + scanSize
            val bottom = top + scanSize
            
            // Dimmed Background
            with(drawContext.canvas.nativeCanvas) {
                val checkPoint = saveLayer(null, null)
                drawRect(Color.Black.copy(alpha = 0.6f))
                
                // Cutout
                drawRoundRect(
                    topLeft = Offset(left, top),
                    size = Size(scanSize, scanSize),
                    cornerRadius = CornerRadius(40f, 40f),
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear
                )
                restoreToCount(checkPoint)
            }

            // Corner Borders
            val strokeW = 6.dp.toPx()
            val cornerLen = 30.dp.toPx()
            
            drawPath(
                path = Path().apply {
                    // Top Left
                    moveTo(left, top + cornerLen)
                    quadraticBezierTo(left, top, left + cornerLen, top)
                    // Top Right
                    moveTo(right - cornerLen, top)
                    quadraticBezierTo(right, top, right, top + cornerLen)
                    // Bottom Right
                    moveTo(right, bottom - cornerLen)
                    quadraticBezierTo(right, bottom, right - cornerLen, bottom)
                    // Bottom Left
                    moveTo(left + cornerLen, bottom)
                    quadraticBezierTo(left, bottom, left, bottom - cornerLen)
                },
                color = primaryColor,
                style = Stroke(width = strokeW, cap = StrokeCap.Round) 
            )

            // Laser Animation (only if idle)
            if (feedbackState == null) {
                val lineY = top + (scanSize * anim)
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, primaryColor, Color.Transparent),
                        start = Offset(left, lineY),
                        end = Offset(right, lineY)
                    ),
                    start = Offset(left, lineY),
                    end = Offset(right, lineY),
                    strokeWidth = 4.dp.toPx()
                )
            }
        }

        // Feedback Icon & Text Centered in Box
        if (feedbackState != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-40).dp), // Adjust for cutout offset
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (feedbackState.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, CircleShape)
                        .padding(4.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = feedbackState.message,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .background(primaryColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                if (feedbackState.studentId != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = feedbackState.studentId,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ManualEntryCard(
    subjectName: String?,
    viewModel: AttendanceViewModel,
    onSelectSubject: () -> Unit = {}
) {
    var studentIdentifier by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("present") }
    val message by viewModel.message.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp, // Added elevation for a premium feel
        backgroundColor = MaterialTheme.colors.surface // Use theme surface color
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start, // Align content to start for better text alignment
            verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing
        ) {
            // Title and description
            Text(
                text = "Manual Attendance Entry",
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.ExtraBold),
                color = PrimaryBlue
            )
            Text(
                text = "Effortlessly record student attendance by inputting details manually.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(16.dp))

            // Subject Selector
            Text(
                text = "Selected Subject",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSubject() },
                shape = RoundedCornerShape(12.dp),
                elevation = 2.dp,
                border = BorderStroke(
                    1.dp,
                    if (subjectName == null) WarningYellow else Color.Transparent // Changed border to Transparent when selected
                ),
                backgroundColor = MaterialTheme.colors.surface // Always white background for the selector card
            ) {
                Row(
                    modifier = Modifier.padding(16.dp), // Increased padding for better spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Class,
                        contentDescription = "Subject",
                        tint = if (subjectName == null) WarningYellow else PrimaryBlue // Tint icon orange if no subject
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subjectName ?: "Tap to Select Subject",
                            fontWeight = FontWeight.Bold,
                            color = if (subjectName == null) WarningYellow else MaterialTheme.colors.onSurface // Text color orange if no subject
                        )
                        if (subjectName == null) {
                            Text(
                                text = "A subject must be selected to record attendance",
                                style = MaterialTheme.typography.caption,
                                color = WarningYellow // Warning text also orange
                            )
                        } else {
                             Text(
                                text = "Currently selected class",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Subject", tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
            }

            // Student Identifier
            OutlinedTextField(
                value = studentIdentifier,
                onValueChange = { studentIdentifier = it },
                label = { Text("Student Name or ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    textColor = MaterialTheme.colors.onSurface
                ),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                singleLine = true // Prevent text from overlapping
            )

            // Status Chips
            Text(
                text = "Attendance Status",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("present", "late", "absent").forEach { opt ->
                    val isSel = status == opt
                    val chipColors = when (opt) {
                        "present" -> SecondaryTeal
                        "late" -> WarningYellow
                        "absent" -> Color.Red
                        else -> Color.Gray
                    }

                    FilterChip(
                        selected = isSel,
                        onClick = { status = opt },
                        colors = ChipDefaults.filterChipColors(
                            selectedBackgroundColor = chipColors.copy(alpha = 0.2f),
                            selectedContentColor = chipColors,
                            backgroundColor = MaterialTheme.colors.surface,
                            contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f) // Unselected content color
                        ),
                        shape = PillShape,
                        border = BorderStroke(1.dp, if (isSel) chipColors else MaterialTheme.colors.onSurface.copy(alpha = 0.1f)),
                        modifier = Modifier.weight(1f) // Distribute chips evenly
                    ) {
                        Text(
                            text = opt.replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(), // Make text fill available width
                            textAlign = TextAlign.Center // Center align the text
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Record Button
            Button(
                onClick = {
                    if (subjectName != null) {
                        viewModel.recordManual(studentIdentifier, status, "subject", subjectName)
                        studentIdentifier = "" // Clear after save
                    } else {
                        onSelectSubject()
                    }
                },
                shape = PillShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (subjectName != null && studentIdentifier.isNotBlank()) PrimaryBlue else Color.Gray,
                    contentColor = Color.White
                ),
                enabled = subjectName != null && studentIdentifier.isNotBlank(),
                elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Record Attendance",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Feedback Message
            if (message != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 0.dp, // Removed shadow
                    backgroundColor = if (message!!.startsWith("Error")) Color.Red.copy(alpha = 0.1f) else Color.Transparent // Transparent for success
                ) {
                    Text(
                        text = message!!,
                        color = if (message!!.startsWith("Error")) Color.Red else SecondaryTeal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(12.dp), // Fill width and padding
                        textAlign = TextAlign.Center // Centered text
                    )
                }
            }
        }
    }
} 

data class ScanFeedback(
    val isSuccess: Boolean,
    val message: String,
    val studentId: String? = null
)

// processes an ImageProxy with ML Kit scanner and calls onDetected with raw string (or null)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(scanner: com.google.mlkit.vision.barcode.BarcodeScanner, imageProxy: ImageProxy, onDetected: (String?) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val first = barcodes.firstOrNull()
                first?.rawValue?.let { onDetected(it) }
            }
            .addOnFailureListener { }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}