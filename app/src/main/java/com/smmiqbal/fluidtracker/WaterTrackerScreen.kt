package com.smmiqbal.fluidtracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluidTrackerMainScreen(viewModel: WaterViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val count by viewModel.glassCount.collectAsState()
    val totalVolumeMl by viewModel.totalVolumeMl.collectAsState()
    val goalVolumeMl by viewModel.goalVolumeMl.collectAsState()
    val healthInsight by viewModel.healthInsight.collectAsState()
    val remindersEnabled by viewModel.remindersEnabled.collectAsState()
    val selectedThemeId by viewModel.selectedThemeId.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()
    val lifetimeDrinks by viewModel.lifetimeDrinks.collectAsState()
    val vitalityScore by viewModel.vitalityScore.collectAsState()
    val streakFreezeCount by viewModel.streakFreezeCount.collectAsState()
    val intakeLogs by viewModel.intakeLogs.collectAsState()
    val weeklyHistory by viewModel.weeklyHistory.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var activeScreenTab by remember { mutableStateOf(0) } // 0 = Hydro (Image 1), 1 = Analytics & Streak (Image 2)
    var selectedTimeframeTab by remember { mutableStateOf(0) } // 0 = D (Day), 1 = W (Week), 2 = M (Month)
    var selectedVolume by remember { mutableStateOf(availableVolumes[0]) } // 250ml default

    var showAboutDialog by remember { mutableStateOf(false) }
    var showGoalReachedDialog by remember { mutableStateOf(false) }
    var showHydrationWrapped by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showInsightDialog by remember { mutableStateOf(false) }
    var showCustomIntakeDialog by remember { mutableStateOf(false) }
    var showGoalAdjustDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf(false) }
    var showLegalTermsDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentTheme = AppTheme.values().find { it.id == selectedThemeId } ?: AppTheme.CELESTIAL

    // Hardware Accelerometer / Gyroscope Physics for Smooth Fluid Tilt
    var rawTiltX by remember { mutableStateOf(0f) }
    var sloshAmplitude by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        var lastTilt = 0f
        var lastTime = System.currentTimeMillis()
        var filteredTilt = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val raw = -it.values[0] * 2.8f
                    val currentTilt = raw.coerceIn(-16f, 16f)
                    // Low-pass filter for silky smooth fluid motion without hand jitter
                    filteredTilt = filteredTilt * 0.78f + currentTilt * 0.22f
                    val now = System.currentTimeMillis()
                    val dt = (now - lastTime).coerceAtLeast(1)
                    val velocity = (filteredTilt - lastTilt) / dt
                    
                    // Add slosh momentum on sudden tilts
                    if (abs(velocity) > 0.04f) {
                        sloshAmplitude = (sloshAmplitude + abs(velocity) * 160f).coerceIn(0f, 36f)
                    }

                    rawTiltX = filteredTilt
                    lastTilt = filteredTilt
                    lastTime = now
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis {
                sloshAmplitude *= 0.94f // Exponential spring decay synced with display refresh rate
                if (sloshAmplitude < 0.04f) sloshAmplitude = 0f
            }
        }
    }

    val smoothTilt by animateFloatAsState(
        targetValue = rawTiltX,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessLow),
        label = "smoothLiquidTilt"
    )

    val smoothSlosh by animateFloatAsState(
        targetValue = sloshAmplitude,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "smoothLiquidSlosh"
    )

    // Parabolic Droplet & Vessel Squash Animation Physics
    var isDropletFlying by remember { mutableStateOf(false) }
    val dropletProgress = remember { Animatable(0f) }
    var isVesselSquashing by remember { mutableStateOf(false) }

    fun handleAddWater(volumeMl: Int) {
        if (isDropletFlying) return
        coroutineScope.launch {
            isDropletFlying = true
            dropletProgress.snapTo(0f)

            dropletProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )

            isDropletFlying = false
            isVesselSquashing = true
            viewModel.addIntake(volumeMl)
            SoundHelper.playWaterDropSound()
            SoundHelper.triggerHapticFeedback(context)

            delay(130)
            isVesselSquashing = false
        }
    }

    // Android 13+ Notification Permission Check
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.scheduleReminders(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(totalVolumeMl, goalVolumeMl) {
        if (totalVolumeMl >= goalVolumeMl && totalVolumeMl - selectedVolume.ml < goalVolumeMl) {
            showGoalReachedDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = currentTheme.bgBase
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Ambient Velvet Background Glows
            AmbientVelvetGlows(
                topGlowColor = currentTheme.heroGradient[0],
                accentGlowColor = currentTheme.accentColor
            )

            // Main View Switcher
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // TOP NAV HEADER
                TopNavHeaderBar(
                    activeTab = activeScreenTab,
                    selectedTimeframe = selectedTimeframeTab,
                    onSelectTimeframe = { selectedTimeframeTab = it },
                    currentTheme = currentTheme,
                    onOpenAbout = { showAboutDialog = true },
                    onOpenSettings = { showSettingsSheet = true },
                    onOpenGoalAdjust = { showGoalAdjustDialog = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (activeScreenTab == 0) {
                    // TAB 0: IMMERSIVE WATER UI (Reference: Image 1)
                    HydroLiquidScreenView(
                        totalVolumeMl = totalVolumeMl,
                        goalVolumeMl = goalVolumeMl,
                        vitalityScore = vitalityScore,
                        streakDays = streakDays,
                        currentTheme = currentTheme,
                        tiltAngle = smoothTilt,
                        sloshAmplitude = smoothSlosh,
                        isVesselSquashing = isVesselSquashing,
                        selectedVolume = selectedVolume,
                        onSelectVolume = { selectedVolume = it },
                        onAddWater = { handleAddWater(selectedVolume.ml) },
                        onCustomIntake = { showCustomIntakeDialog = true },
                        onReset = {
                            viewModel.resetCount()
                            Toast.makeText(context, "Today's intake reset.", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // TAB 1: STREAK & HISTORY ANALYTICS VIEW (Reference: Image 2)
                    StreakAndHistoryView(
                        totalVolumeMl = totalVolumeMl,
                        goalVolumeMl = goalVolumeMl,
                        streakDays = streakDays,
                        vitalityScore = vitalityScore,
                        timeframe = selectedTimeframeTab,
                        weeklyHistory = weeklyHistory,
                        intakeLogs = intakeLogs,
                        currentTheme = currentTheme,
                        userName = userName,
                        onEditUser = { showEditUserDialog = true },
                        onDeleteIntake = { id ->
                            viewModel.deleteIntake(id)
                            Toast.makeText(context, "Intake entry removed.", Toast.LENGTH_SHORT).show()
                        },
                        onShowWrapped = { showHydrationWrapped = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HEALTH INSIGHT GLASS STRIP
                VitalityInsightStrip(
                    insight = healthInsight,
                    currentTheme = currentTheme,
                    onClick = { showInsightDialog = true }
                )

                // Extra padding for bottom floating dock
                Spacer(modifier = Modifier.height(105.dp))
            }

            // FLOATING BOTTOM GLASS DOCK (Tabs + Quick Add FAB)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
            ) {
                FloatingGlassDockBar(
                    activeTab = activeScreenTab,
                    onSelectTab = { activeScreenTab = it },
                    currentTheme = currentTheme,
                    onQuickAdd = { handleAddWater(selectedVolume.ml) },
                    onOpenSettings = { showSettingsSheet = true }
                )
            }

            // Parabolic Droplet Flying Particle
            if (isDropletFlying) {
                FlyingLiquidDroplet(
                    progress = dropletProgress.value,
                    themeColor = currentTheme.accentColor
                )
            }
        }
    }

    // MODAL BOTTOM SHEETS & DIALOGS
    if (showSettingsSheet) {
        SettingsBottomSheet(
            sheetState = sheetState,
            currentTheme = currentTheme,
            remindersEnabled = remindersEnabled,
            streakFreezes = streakFreezeCount,
            goalVolumeMl = goalVolumeMl,
            userName = userName,
            onEditUser = {
                showSettingsSheet = false
                showEditUserDialog = true
            },
            onOpenLegal = {
                showSettingsSheet = false
                showLegalTermsDialog = true
            },
            onToggleReminders = { viewModel.toggleReminders(it) },
            onSelectTheme = { viewModel.selectTheme(it) },
            onAdjustGoal = { showGoalAdjustDialog = true },
            onUseFreeze = {
                viewModel.useStreakFreeze { success ->
                    val msg = if (success) "Streak freeze applied. Streak protected!" else "No streak freezes remaining."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(
            currentTheme = currentTheme,
            onOpenLegal = {
                showAboutDialog = false
                showLegalTermsDialog = true
            },
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showGoalReachedDialog) {
        GoalCelebrationDialog(
            vitalityEarned = 50,
            onDismiss = { showGoalReachedDialog = false }
        )
    }

    if (showHydrationWrapped) {
        HydrationWrappedDialog(
            onDismiss = { showHydrationWrapped = false },
            totalVolumeMl = totalVolumeMl,
            streakDays = streakDays,
            currentTheme = currentTheme,
            userName = userName
        )
    }

    if (showInsightDialog) {
        InsightsDialog(
            insight = healthInsight,
            vitalityScore = vitalityScore,
            streakDays = streakDays,
            currentTheme = currentTheme,
            onDismiss = { showInsightDialog = false }
        )
    }

    if (showCustomIntakeDialog) {
        CustomIntakeDialog(
            currentTheme = currentTheme,
            onConfirm = { customMl ->
                handleAddWater(customMl)
                showCustomIntakeDialog = false
            },
            onDismiss = { showCustomIntakeDialog = false }
        )
    }

    if (showGoalAdjustDialog) {
        GoalAdjustmentDialog(
            currentGoalMl = goalVolumeMl,
            currentTheme = currentTheme,
            onConfirm = { newGoal ->
                viewModel.setGoalVolume(newGoal)
                showGoalAdjustDialog = false
                Toast.makeText(context, "Daily goal updated to ${newGoal}ml", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showGoalAdjustDialog = false }
        )
    }

    if (showEditUserDialog) {
        EditUsernameDialog(
            currentName = userName,
            currentTheme = currentTheme,
            onConfirm = { newName ->
                viewModel.setUserName(newName)
                showEditUserDialog = false
                Toast.makeText(context, "Hydration profile updated: $newName", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEditUserDialog = false }
        )
    }

    if (showLegalTermsDialog) {
        LegalTermsAndPrivacyDialog(
            currentTheme = currentTheme,
            onDismiss = { showLegalTermsDialog = false }
        )
    }
}

// =========================================================================
// 1. TOP NAVIGATION HEADER
// =========================================================================
@Composable
fun TopNavHeaderBar(
    activeTab: Int,
    selectedTimeframe: Int,
    onSelectTimeframe: (Int) -> Unit,
    currentTheme: AppTheme,
    onOpenAbout: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGoalAdjust: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 3D App Logo Badge (Pure Water Droplet, Borderless)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF060910))
                .clickable { onOpenAbout() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_round),
                contentDescription = "Fluid Tracker Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Center: Contextual Pill (Image 1 status pill vs Image 2 D/W/M toggle)
        if (activeTab == 0) {
            // Status Pill (Pure Water • 2,000 ml target)
            Box(
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(21.dp))
                    .clickable { onOpenGoalAdjust() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = currentTheme.accentColor, radius = 4.dp.toPx())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pure Water • Target Goal",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        } else {
            // Segmented Liquid Glass D • W • M Switcher (Reference: Image 2)
            Row(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(21.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("D", "W", "M").forEachIndexed { idx, label ->
                    val isSel = selectedTimeframe == idx
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSel) currentTheme.surfaceColor.copy(alpha = 0.95f) else Color.Transparent
                            )
                            .border(
                                width = if (isSel) 1.dp else 0.dp,
                                color = if (isSel) currentTheme.accentColor.copy(alpha = 0.7f) else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onSelectTimeframe(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isSel) Color.White else Color.White.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }

        // Right: Options Button (Three dots "...")
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                .clickable { onOpenSettings() },
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Canvas(modifier = Modifier.size(4.dp)) {
                        drawCircle(color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. HYDRO LIQUID SCREEN VIEW (Reference: Image 1)
// =========================================================================
@Composable
fun HydroLiquidScreenView(
    totalVolumeMl: Int,
    goalVolumeMl: Int,
    vitalityScore: Int,
    streakDays: Int,
    currentTheme: AppTheme,
    tiltAngle: Float,
    sloshAmplitude: Float,
    isVesselSquashing: Boolean,
    selectedVolume: VolumeOption,
    onSelectVolume: (VolumeOption) -> Unit,
    onAddWater: () -> Unit,
    onCustomIntake: () -> Unit,
    onReset: () -> Unit
) {
    val targetFill = (totalVolumeMl.toFloat() / goalVolumeMl.toFloat()).coerceIn(0f, 1f)
    val animatedFillFraction by animateFloatAsState(
        targetValue = targetFill,
        animationSpec = spring(
            dampingRatio = 0.68f, // Silky liquid buoyancy bob & rise
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedFluidFill"
    )
    val fillFraction = animatedFillFraction
    val percentageInt = ((totalVolumeMl.toFloat() / goalVolumeMl.toFloat()) * 100).toInt()
    val ouncesTaken = totalVolumeMl * 0.033814f

    // Live ticking countdown timer (Image 1 "TIME LEFT 11 : 55 46" format)
    var remainingSeconds by remember { mutableStateOf(calculateInitialTimeLeft()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
            if (remainingSeconds == 0) {
                remainingSeconds = 90 * 60 // reset interval
            }
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val countdownText = String.format(Locale.US, "%02d : %02d %02d", hours, minutes, seconds)

    // Dynamic greeting based on current local hour (Image 1 "GOOD MORNING" format)
    val currentHour = remember { LocalTime.now().hour }
    val greetingText = when (currentHour) {
        in 5..11 -> "GOOD MORNING"
        in 12..16 -> "GOOD AFTERNOON"
        in 17..21 -> "GOOD EVENING"
        else -> "GOOD NIGHT"
    }

    // Squash & Stretch bounce effect
    val vesselScaleX by animateFloatAsState(
        targetValue = if (isVesselSquashing) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "vesselScaleX"
    )
    val vesselScaleY by animateFloatAsState(
        targetValue = if (isVesselSquashing) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "vesselScaleY"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MAIN IMMERSIVE LIQUID VESSEL (Image 1 Reference)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(490.dp)
                .scale(scaleX = vesselScaleX, scaleY = vesselScaleY)
                .clip(RoundedCornerShape(36.dp))
                .background(Color(0xFF131722))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.08f),
                            currentTheme.accentColor.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
        ) {
            // 3D Liquid Canvas Wave Simulation
            WaterSurfaceSimulationCanvas(
                fillFraction = fillFraction,
                tiltAngle = tiltAngle,
                sloshAmplitude = sloshAmplitude,
                accentColor = currentTheme.accentColor,
                heroGradient = currentTheme.heroGradient
            )

            // Content Overlay (Typography & Readouts matching Image 1)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Huge Minimalist Percentage (Image 1 Reference "72%")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentageInt%",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 78.sp,
                            fontWeight = FontWeight.ExtraLight,
                            letterSpacing = (-2).sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = Color.White
                    )
                }

                // Middle: Time Left Stopwatch & Live Countdown (Image 1 Reference)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stopwatch icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            // Stopwatch outer circle
                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = Offset(size.width / 2f, size.height * 0.55f),
                                style = Stroke(width = 1.6.dp.toPx())
                            )
                            // Top button
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width / 2f, 1.dp.toPx()),
                                end = Offset(size.width / 2f, 4.dp.toPx()),
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // Clock hand
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width / 2f, size.height * 0.55f),
                                end = Offset(size.width * 0.65f, size.height * 0.45f),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    Text(
                        text = "TIME LEFT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 11.sp
                        ),
                        color = Color.White.copy(alpha = 0.90f)
                    )

                    Text(
                        text = countdownText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 28.sp,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                }

                // Bottom: Greeting & Volume Taken (Image 1 Reference)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 13.sp
                        ),
                        color = Color.White.copy(alpha = 0.95f)
                    )

                    Text(
                        text = "$totalVolumeMl ml taken",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )

                    Text(
                        text = String.format(Locale.US, "%.1f Oz taken • Goal %d ml", ouncesTaken, goalVolumeMl),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // DAILY GLASS TUMBLERS ROW (Reference: Image 2)
        val cupsTotal = 8
        val cupsFilled = (totalVolumeMl / (goalVolumeMl / cupsTotal).coerceAtLeast(1)).coerceIn(0, cupsTotal)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(currentTheme.surfaceColor.copy(alpha = 0.6f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$cupsFilled/$cupsTotal Cups Taken",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.90f)
                    )
                    Text(
                        text = "${goalVolumeMl}ml Daily Target",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = currentTheme.accentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until cupsTotal) {
                        val isFilled = i < cupsFilled
                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 36.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                                .background(
                                    if (isFilled) currentTheme.accentColor.copy(alpha = 0.85f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .border(
                                    width = 1.2.dp,
                                    color = if (isFilled) currentTheme.accentColor else Color.White.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (isFilled) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                        .background(Color.White.copy(alpha = 0.25f))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // QUICK VOLUME SELECTOR PILLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableVolumes.forEach { vol ->
                val isSelected = vol.ml == selectedVolume.ml
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) currentTheme.surfaceColor else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) currentTheme.accentColor else Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectVolume(vol) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = vol.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Custom volume button "+"
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .clickable { onCustomIntake() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width / 2f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

// =========================================================================
// 3. STREAK & HISTORY ANALYTICS VIEW (Reference: Image 2)
// =========================================================================
@Composable
fun StreakAndHistoryView(
    totalVolumeMl: Int,
    goalVolumeMl: Int,
    streakDays: Int,
    vitalityScore: Int,
    timeframe: Int,
    weeklyHistory: List<DayHistory>,
    intakeLogs: List<IntakeLog>,
    currentTheme: AppTheme,
    userName: String = "S. M. Mahmud Iqbal",
    onEditUser: () -> Unit = {},
    onDeleteIntake: (Long) -> Unit,
    onShowWrapped: () -> Unit
) {
    val completionPct = ((totalVolumeMl.toFloat() / goalVolumeMl.toFloat()) * 100).toInt()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO CIRCULAR PROGRESS CARD (Reference: Image 2 "2,843 of 5,000" Ring)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(currentTheme.surfaceColor.copy(alpha = 0.75f))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(32.dp))
                .padding(vertical = 24.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Circular Arc with Center Metrics
                Box(
                    modifier = Modifier.size(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radius = diameter / 2f
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

                        // Background track arc
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Animated Glowing progress arc
                        val sweep = (totalVolumeMl.toFloat() / goalVolumeMl.toFloat() * 260f).coerceIn(4f, 260f)
                        drawArc(
                            brush = Brush.linearGradient(currentTheme.heroGradient),
                            startAngle = -220f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Center Numbers
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (timeframe == 0) "Today" else if (timeframe == 1) "This Week" else "This Month",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.accentColor
                            )
                        )
                        Text(
                            text = String.format(Locale.US, "%,d", totalVolumeMl),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 42.sp,
                                letterSpacing = (-1).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "of ${String.format(Locale.US, "%,d", goalVolumeMl)} ml",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }

        // 2. HORIZONTAL STAT CAPSULES (Reference: Image 2 Streak, kcal, mi, min pills)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pill 1: Streak
            StatPillCard(
                icon = {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        drawCircle(color = Color(0xFFF97316), radius = 5.dp.toPx())
                    }
                },
                topValue = "$streakDays",
                bottomLabel = "Streak",
                modifier = Modifier.weight(1f)
            )

            // Pill 2: Goal Pct
            StatPillCard(
                icon = null,
                topValue = "$completionPct%",
                bottomLabel = "Goal",
                modifier = Modifier.weight(1f)
            )

            // Pill 3: Vitality PTS
            StatPillCard(
                icon = null,
                topValue = "$vitalityScore",
                bottomLabel = "Vitality",
                modifier = Modifier.weight(1f)
            )

            // Pill 4: Intake Log Count
            StatPillCard(
                icon = null,
                topValue = "${intakeLogs.size}",
                bottomLabel = "Logs",
                modifier = Modifier.weight(1f)
            )
        }

        // HYDRATION WRAPPED EXPORT BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(currentTheme.heroGradient[0].copy(alpha = 0.8f), currentTheme.accentColor.copy(alpha = 0.8f))
                    )
                )
                .clickable { onShowWrapped() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(18.dp)) {
                    val r = 2.2.dp.toPx()
                    val rightTop = Offset(size.width * 0.8f, size.height * 0.25f)
                    val rightBottom = Offset(size.width * 0.8f, size.height * 0.75f)
                    val leftMid = Offset(size.width * 0.25f, size.height * 0.5f)
                    drawLine(Color.White, leftMid, rightTop, strokeWidth = 2.dp.toPx())
                    drawLine(Color.White, leftMid, rightBottom, strokeWidth = 2.dp.toPx())
                    drawCircle(Color.White, r, leftMid)
                    drawCircle(Color.White, r, rightTop)
                    drawCircle(Color.White, r, rightBottom)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Generate Hydration Wrapped",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }

        // 3. 7-DAY SPLINE CURVE HISTORY CARD (Reference: Image 2 Wave Chart)
        SevenDaySplineChartCard(
            weeklyHistory = weeklyHistory,
            goalVolumeMl = goalVolumeMl,
            currentTheme = currentTheme
        )

        // 4. USER CARD & INTAKE ACTIVITY LOG (Reference: Image 2 User Row & List)
        ActivityLogSection(
            totalVolumeMl = totalVolumeMl,
            intakeLogs = intakeLogs,
            currentTheme = currentTheme,
            userName = userName,
            onEditUser = onEditUser,
            onDeleteIntake = onDeleteIntake
        )
    }
}

// 7-Day Spline Chart Card (Reference: Image 2 TUE..MON Chart)
@Composable
fun SevenDaySplineChartCard(
    weeklyHistory: List<DayHistory>,
    goalVolumeMl: Int,
    currentTheme: AppTheme
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF141720))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Days of the week row (TUE, WED, THU, FRI, SAT, SUN, MON)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeklyHistory.forEach { day ->
                    if (day.isToday) {
                        // Highlighted active day capsule (like MON in Image 2)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = Color.Black
                            )
                        }
                    } else {
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White.copy(alpha = 0.50f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Smooth Bézier Spline Wave Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val w = size.width
                val h = size.height
                val count = weeklyHistory.size
                if (count < 2) return@Canvas

                val stepX = w / (count - 1)
                val maxVal = (goalVolumeMl * 1.25f).coerceAtLeast(2000f)

                // Compute points
                val points = weeklyHistory.mapIndexed { idx, item ->
                    val x = idx * stepX
                    val ratio = (item.volumeMl.toFloat() / maxVal).coerceIn(0.08f, 0.95f)
                    val y = h * (1f - ratio)
                    Offset(x, y)
                }

                // Dotted Target Baseline (at 100% goal)
                val goalY = h * (1f - (goalVolumeMl.toFloat() / maxVal))
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, goalY),
                    end = Offset(w, goalY),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Create Spline Path
                val splinePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }

                // Gradient Fill underneath the wave
                val fillPath = Path().apply {
                    addPath(splinePath)
                    lineTo(points.last().x, h)
                    lineTo(points.first().x, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            currentTheme.accentColor.copy(alpha = 0.35f),
                            currentTheme.accentColor.copy(alpha = 0.02f)
                        ),
                        startY = 0f,
                        endY = h
                    )
                )

                // Luminous Spline Curve Line
                drawPath(
                    path = splinePath,
                    color = currentTheme.accentColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Circular Nodes at each day
                points.forEachIndexed { idx, pt ->
                    val isToday = idx == points.size - 1
                    if (isToday) {
                        // Double ring for today (like Image 2 node)
                        drawCircle(
                            color = currentTheme.accentColor,
                            radius = 9.dp.toPx(),
                            center = pt,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                    } else {
                        drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = pt)
                    }
                }
            }
        }
    }
}

// Activity Log & Intake History Section (Reference: Image 2 User Card & Entries)
@Composable
fun ActivityLogSection(
    totalVolumeMl: Int,
    intakeLogs: List<IntakeLog>,
    currentTheme: AppTheme,
    userName: String = "S. M. Mahmud Iqbal",
    onEditUser: () -> Unit = {},
    onDeleteIntake: (Long) -> Unit
) {
    val initials = remember(userName) {
        val parts = userName.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "ME"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // User Profile Summary Card (Reference: Image 2 Maliyah Brown Me Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                .clickable { onEditUser() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF472B6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(currentTheme.accentColor.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Me",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentTheme.accentColor
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Tap to customize username",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = currentTheme.accentColor.copy(alpha = 0.85f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%,d ml", totalVolumeMl),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = currentTheme.accentColor
                    )
                }
            }
        }

        // Intake Logs List
        Text(
            text = "Today's Intake History",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.70f)
            ),
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )

        if (intakeLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No intake recorded yet today. Tap + to log pure water.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        } else {
            intakeLogs.forEach { log ->
                IntakeLogRowItem(
                    log = log,
                    currentTheme = currentTheme,
                    onDelete = { onDeleteIntake(log.id) }
                )
            }
        }
    }
}

@Composable
fun IntakeLogRowItem(
    log: IntakeLog,
    currentTheme: AppTheme,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141720))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Water drop badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(currentTheme.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, 0f)
                            cubicTo(
                                size.width * 0.9f, size.height * 0.45f,
                                size.width, size.height * 0.75f,
                                size.width / 2f, size.height
                            )
                            cubicTo(
                                0f, size.height * 0.75f,
                                size.width * 0.1f, size.height * 0.45f,
                                size.width / 2f, 0f
                            )
                            close()
                        }
                        drawPath(path, color = currentTheme.accentColor)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${log.volumeMl} ml Pure Water",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${log.timeStr} • +25 Vitality",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Undo / Delete button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(size.width, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

// Stat Pill Card (Image 2 horizontal capsule reference)
@Composable
fun StatPillCard(
    icon: (@Composable () -> Unit)?,
    topValue: String,
    bottomLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141720))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = topValue,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }
            Text(
                text = bottomLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.50f)
                )
            )
        }
    }
}

// =========================================================================
// 4. WATER SURFACE SIMULATION CANVAS (Dual Wave + Specular Meniscus)
// =========================================================================
@Composable
fun WaterSurfaceSimulationCanvas(
    fillFraction: Float,
    tiltAngle: Float,
    sloshAmplitude: Float,
    accentColor: Color,
    heroGradient: List<Color>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluidMotion")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val baseY = h * (1f - (fillFraction * 0.72f).coerceIn(0.12f, 0.85f))
        val tiltOffset = (tiltAngle / 16f) * (w * 0.14f)

        // Smooth Spline Wave Generator Function
        fun generateSmoothFluidPath(
            invertTilt: Boolean,
            amplitude: Float,
            freq: Float,
            harmonicFreq: Float,
            harmonicAmp: Float,
            phaseOffset: Float
        ): Path {
            val path = Path()
            val segments = 28
            val step = w / segments
            val pts = ArrayList<Offset>(segments + 1)
            val effTilt = if (invertTilt) -tiltOffset else tiltOffset

            for (i in 0..segments) {
                val x = i * step
                val normX = x / w
                val sloshEffect = sin(PI.toFloat() * normX) * sloshAmplitude.dp.toPx()
                val wave1 = sin(waveOffset * 0.95f + (i * freq) + phaseOffset) * amplitude
                val wave2 = cos(waveOffset * 1.35f + (i * harmonicFreq) + phaseOffset) * harmonicAmp
                val tiltY = effTilt * (1f - 2f * normX)
                val currentY = baseY + tiltY + wave1 + wave2 + sloshEffect
                pts.add(Offset(x, currentY))
            }

            path.moveTo(pts[0].x, pts[0].y)
            for (i in 0 until pts.size - 1) {
                val p0 = pts[i]
                val p1 = pts[i + 1]
                val midX = (p0.x + p1.x) / 2f
                val midY = (p0.y + p1.y) / 2f
                path.quadraticTo(p0.x, p0.y, midX, midY)
            }
            path.lineTo(pts.last().x, pts.last().y)
            path.lineTo(w, h)
            path.lineTo(0f, h)
            path.close()
            return path
        }

        // 1. Back Wave Layer (Lighter translucent cyan/accent)
        val backWavePath = generateSmoothFluidPath(
            invertTilt = false,
            amplitude = 7.5.dp.toPx(),
            freq = 0.38f,
            harmonicFreq = 0.58f,
            harmonicAmp = 2.4.dp.toPx(),
            phaseOffset = 0f
        )

        drawPath(
            path = backWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.35f),
                    accentColor.copy(alpha = 0.15f)
                ),
                startY = baseY - 20f,
                endY = h
            )
        )

        // 2. Main Foreground Fluid Body (Deep, rich ocean/sunset liquid)
        val foreWavePath = generateSmoothFluidPath(
            invertTilt = true,
            amplitude = 6.2.dp.toPx(),
            freq = 0.42f,
            harmonicFreq = 0.65f,
            harmonicAmp = 2.0.dp.toPx(),
            phaseOffset = 1.25f
        )

        drawPath(
            path = foreWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.90f),
                    heroGradient[0].copy(alpha = 0.85f),
                    Color(0xFF0C1929)
                ),
                startY = baseY - 10f,
                endY = h
            )
        )

        // 3. Specular Meniscus Surface Highlight Line (Continuous Bézier Spline)
        val meniscusPath = Path().apply {
            val segments = 28
            val step = w / segments
            val pts = ArrayList<Offset>(segments + 1)

            for (i in 0..segments) {
                val x = i * step
                val normX = x / w
                val sloshEffect = sin(PI.toFloat() * normX) * (sloshAmplitude.dp.toPx() * 1.15f)
                val wave1 = sin(waveOffset * 0.95f + (i * 0.42f) + 1.25f) * 6.2.dp.toPx()
                val wave2 = cos(waveOffset * 1.35f + (i * 0.65f) + 1.25f) * 2.0.dp.toPx()
                val tiltY = (-tiltOffset) * (1f - 2f * normX)
                val currentY = baseY + tiltY + wave1 + wave2 + sloshEffect
                pts.add(Offset(x, currentY))
            }

            moveTo(pts[0].x, pts[0].y)
            for (i in 0 until pts.size - 1) {
                val p0 = pts[i]
                val p1 = pts[i + 1]
                val midX = (p0.x + p1.x) / 2f
                val midY = (p0.y + p1.y) / 2f
                quadraticTo(p0.x, p0.y, midX, midY)
            }
            lineTo(pts.last().x, pts.last().y)
        }

        drawPath(
            path = meniscusPath,
            color = Color.White.copy(alpha = 0.92f),
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )

        // 4. Concentric Fluid Ripple Rings (Reference: Image 1 Right)
        val ringCenter = Offset(w / 2f, h * 0.44f)
        for (rIndex in 1..4) {
            val baseR = rIndex * 36.dp.toPx()
            val pulseR = baseR + sin(waveOffset + rIndex * 0.8f) * 4.dp.toPx()
            val ringAlpha = (0.08f + 0.05f * sin(waveOffset + rIndex * 0.7f)).coerceIn(0.03f, 0.16f)
            drawCircle(
                color = Color.White.copy(alpha = ringAlpha),
                radius = pulseR,
                center = ringCenter,
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), waveOffset * 10f)
                )
            )
        }

        // 5. Rising Ethereal Bubbles (Reference: Image 1 Right)
        for (b in 0..5) {
            val bubblePhase = (waveOffset * 0.75f + b * 1.15f) % (2 * PI.toFloat())
            val bubbleNormY = bubblePhase / (2 * PI.toFloat())
            val bubbleY = h - (h - (baseY + 20f)) * bubbleNormY
            val bubbleX = (w * (0.22f + b * 0.12f)) + sin(waveOffset * 1.4f + b) * 10.dp.toPx()
            val bubbleRadius = (3.5f + (b % 4) * 2.2f).dp.toPx()
            if (bubbleY > baseY + 10f) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.20f),
                    radius = bubbleRadius,
                    center = Offset(bubbleX, bubbleY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = bubbleRadius * 0.35f,
                    center = Offset(bubbleX - bubbleRadius * 0.35f, bubbleY - bubbleRadius * 0.35f)
                )
            }
        }
    }
}

// =========================================================================
// 5. FLOATING BOTTOM GLASS DOCK (Reference: Image 1 & 2 Docks)
// =========================================================================
@Composable
fun FloatingGlassDockBar(
    activeTab: Int,
    onSelectTab: (Int) -> Unit,
    currentTheme: AppTheme,
    onQuickAdd: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(Color(0xFF0F131D).copy(alpha = 0.88f))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.25f)
                    )
                ),
                RoundedCornerShape(34.dp)
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Hydro View & Analytics View
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 0: Hydro Tank (Image 1 Droplet Icon)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (activeTab == 0) currentTheme.accentColor.copy(alpha = 0.22f) else Color.Transparent
                        )
                        .border(
                            width = if (activeTab == 0) 1.2.dp else 0.dp,
                            color = if (activeTab == 0) currentTheme.accentColor else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelectTab(0) },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, 0f)
                            cubicTo(
                                size.width * 0.9f, size.height * 0.45f,
                                size.width, size.height * 0.75f,
                                size.width / 2f, size.height
                            )
                            cubicTo(
                                0f, size.height * 0.75f,
                                size.width * 0.1f, size.height * 0.45f,
                                size.width / 2f, 0f
                            )
                            close()
                        }
                        drawPath(
                            path = path,
                            color = if (activeTab == 0) currentTheme.accentColor else Color.White.copy(alpha = 0.55f)
                        )
                    }
                }

                // Tab 1: Analytics & History (Image 2 Chart Icon)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (activeTab == 1) currentTheme.accentColor.copy(alpha = 0.22f) else Color.Transparent
                        )
                        .border(
                            width = if (activeTab == 1) 1.2.dp else 0.dp,
                            color = if (activeTab == 1) currentTheme.accentColor else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelectTab(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        // 3 Bar charts / wave icon
                        val barW = 3.5.dp.toPx()
                        drawLine(
                            color = if (activeTab == 1) currentTheme.accentColor else Color.White.copy(alpha = 0.55f),
                            start = Offset(2.dp.toPx(), size.height),
                            end = Offset(2.dp.toPx(), size.height * 0.55f),
                            strokeWidth = barW,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = if (activeTab == 1) currentTheme.accentColor else Color.White.copy(alpha = 0.55f),
                            start = Offset(size.width / 2f, size.height),
                            end = Offset(size.width / 2f, size.height * 0.25f),
                            strokeWidth = barW,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = if (activeTab == 1) currentTheme.accentColor else Color.White.copy(alpha = 0.55f),
                            start = Offset(size.width - 2.dp.toPx(), size.height),
                            end = Offset(size.width - 2.dp.toPx(), size.height * 0.40f),
                            strokeWidth = barW,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Right Group: Settings & Prominent Quick Add FAB (Image 2 "+" Circle)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { onOpenSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = 6.dp.toPx(),
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }

                // Quick Add Floating Action Button (Prominent "+" Button matching Image 2)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    currentTheme.accentColor,
                                    currentTheme.heroGradient[0]
                                )
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable { onQuickAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width / 2f, 2.dp.toPx()),
                            end = Offset(size.width / 2f, size.height - 2.dp.toPx()),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(2.dp.toPx(), size.height / 2f),
                            end = Offset(size.width - 2.dp.toPx(), size.height / 2f),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. HEALTH INSIGHT STRIP
// =========================================================================
@Composable
fun VitalityInsightStrip(
    insight: String,
    currentTheme: AppTheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(10.dp)) {
                drawCircle(color = currentTheme.accentColor, radius = 4.dp.toPx())
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = insight,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                ),
                color = Color.White.copy(alpha = 0.80f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =========================================================================
// 7. DIALOGS & BOTTOM SHEETS
// =========================================================================

// Custom Intake Volume Dialog
@Composable
fun CustomIntakeDialog(
    currentTheme: AppTheme,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var customVolume by remember { mutableStateOf(300f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Custom Fluid Intake",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${customVolume.toInt()} ml",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = currentTheme.accentColor
                    )
                )

                Slider(
                    value = customVolume,
                    onValueChange = { customVolume = it },
                    valueRange = 50f..1200f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = currentTheme.accentColor,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = { onConfirm(customVolume.toInt()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Log Water", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Daily Goal Adjustment Dialog
@Composable
fun GoalAdjustmentDialog(
    currentGoalMl: Int,
    currentTheme: AppTheme,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var goalVal by remember { mutableStateOf(currentGoalMl.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Daily Hydration Target",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${goalVal.toInt()} ml",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = currentTheme.accentColor
                    )
                )

                Slider(
                    value = goalVal,
                    onValueChange = { goalVal = it },
                    valueRange = 1000f..4000f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = currentTheme.accentColor,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = { onConfirm(goalVal.toInt()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Target", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Settings Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    currentTheme: AppTheme,
    remindersEnabled: Boolean,
    streakFreezes: Int,
    goalVolumeMl: Int,
    userName: String = "S. M. Mahmud Iqbal",
    onEditUser: () -> Unit = {},
    onOpenLegal: () -> Unit = {},
    onToggleReminders: (Boolean) -> Unit,
    onSelectTheme: (String) -> Unit,
    onAdjustGoal: () -> Unit,
    onUseFreeze: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = currentTheme.surfaceColor,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "HYDRATION SETTINGS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Hydration Profile / Username
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onEditUser() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hydration Profile", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("User: $userName", color = currentTheme.accentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("Customize", color = currentTheme.accentColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Target Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onAdjustGoal() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Target", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Current goal: ${goalVolumeMl}ml", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Text("Edit", color = currentTheme.accentColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminders Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hydration Reminders", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Periodic health reminders", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onToggleReminders,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = currentTheme.accentColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Streak Protection Freeze
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Streak Shield Freeze", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("$streakFreezes shields available", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                TextButton(onClick = onUseFreeze) {
                    Text("Use Shield", color = currentTheme.accentColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Agreement & Privacy Policy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onOpenLegal() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("User Agreement & Privacy", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("100% On-Device DataStore • MIT License", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Text("View", color = currentTheme.accentColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 8 Luxury Themes
            Text(
                text = "LIQUID GLASS THEMES (8 BESPOKE STYLES)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            AppTheme.values().forEach { theme ->
                val isSelected = theme.id == currentTheme.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) theme.accentColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) theme.accentColor else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectTheme(theme.id) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(theme.heroGradient))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(theme.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(theme.subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    if (isSelected) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(color = theme.accentColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// About App Dialog (Displays official 3D App Logo)
@Composable
fun AboutAppDialog(
    currentTheme: AppTheme,
    onOpenLegal: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Official 3D Water Droplet Logo (Pure & Borderless)
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Fluid Tracker Logo",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "FLUID TRACKER PRO",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Developed by S. M. Mahmud Iqbal",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = currentTheme.accentColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "3D MINIMALIST LIQUID GLASS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = currentTheme.accentColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Engineered with dual-mode hydro analytics, real-time wave physics, gyroscope accelerometer slosh, live countdown timer, and 7-day spline activity trends.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onOpenLegal,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text("User Agreement & Privacy Policy", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Goal Celebration Dialog
@Composable
fun GoalCelebrationDialog(vitalityEarned: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DAILY TARGET FULFILLED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Optimal cellular hydration achieved for today. Peak cognitive and metabolic performance sustained.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+$vitalityEarned VITALITY POINTS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF34D399)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Insights Dialog
@Composable
fun InsightsDialog(
    insight: String,
    vitalityScore: Int,
    streakDays: Int,
    currentTheme: AppTheme,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HYDRATION METRICS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(label = "ACTIVE STREAK", value = "Day $streakDays")
                    MetricItem(label = "VITALITY SCORE", value = "$vitalityScore PTS")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

// Parabolic flying droplet
@Composable
fun FlyingLiquidDroplet(progress: Float, themeColor: Color) {
    val startX = 0.5f
    val endX = 0.5f
    val startY = 0.90f
    val endY = 0.35f

    val currentX = startX + (endX - startX) * progress
    val currentY = startY - ((startY - endY) * progress) - (sin(progress * PI.toFloat()) * 0.08f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(
                x = (currentX * 300).dp - 150.dp,
                y = (currentY * 700).dp - 350.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val dropletPath = Path().apply {
                moveTo(size.width / 2f, 0f)
                cubicTo(
                    size.width * 0.9f, size.height * 0.45f,
                    size.width, size.height * 0.75f,
                    size.width / 2f, size.height
                )
                cubicTo(
                    0f, size.height * 0.75f,
                    size.width * 0.1f, size.height * 0.45f,
                    size.width / 2f, 0f
                )
                close()
            }
            drawPath(path = dropletPath, color = themeColor)
        }
    }
}

// Ambient Velvet Background Glows
@Composable
fun AmbientVelvetGlows(topGlowColor: Color, accentGlowColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(topGlowColor.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.15f),
                radius = size.width * 0.75f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentGlowColor.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.65f),
                radius = size.width * 0.65f
            )
        )
    }
}

fun calculateInitialTimeLeft(): Int {
    val now = LocalTime.now()
    val nextTargetHour = (now.hour + 2).coerceAtMost(23)
    val remaining = (nextTargetHour - now.hour) * 3600 - now.minute * 60 - now.second
    return remaining.coerceAtLeast(1800)
}
// =========================================================================
// 8. HYDRATION WRAPPED (Shareable Summary Dialog)
// =========================================================================
@Composable
fun HydrationWrappedDialog(
    onDismiss: () -> Unit,
    totalVolumeMl: Int,
    streakDays: Int,
    currentTheme: AppTheme,
    userName: String = "S. M. Mahmud Iqbal"
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "wrappedPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Shareable Story Canvas
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth(0.88f)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(currentTheme.heroGradient[0], currentTheme.bgBase)
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
                    .clickable { /* consume clicks inside card */ }
            ) {
                // Background Waves
                WaterSurfaceSimulationCanvas(
                    fillFraction = 0.52f,
                    tiltAngle = 0f,
                    sloshAmplitude = 12f,
                    accentColor = currentTheme.accentColor,
                    heroGradient = currentTheme.heroGradient
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HYDRATION WRAPPED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(12.dp)) {
                                drawLine(Color.White, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                                drawLine(Color.White, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "A Milestone Reached",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "${totalVolumeMl}ml",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 60.sp
                            ),
                            color = currentTheme.accentColor
                        )
                        Text(
                            text = "Total Hydration Logged Today",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.5.dp, currentTheme.accentColor.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(32.dp)) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFF97316), Color(0xFFEF4444), Color.Transparent)
                                    ),
                                    radius = size.width / 2f
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4.dp.toPx(),
                                    center = Offset(size.width / 2f, size.height / 2f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$streakDays DAY STREAK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }

                    // FUNCTIONAL SHARE TO STORY BUTTON
                    Button(
                        onClick = {
                            StoryShareHelper.shareHydrationStory(
                                context = context,
                                totalVolumeMl = totalVolumeMl,
                                streakDays = streakDays,
                                currentTheme = currentTheme,
                                userName = userName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(54.dp)
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val r = 2.dp.toPx()
                            val rightTop = Offset(size.width * 0.8f, size.height * 0.25f)
                            val rightBottom = Offset(size.width * 0.8f, size.height * 0.75f)
                            val leftMid = Offset(size.width * 0.25f, size.height * 0.5f)
                            drawLine(Color.Black, leftMid, rightTop, strokeWidth = 2.dp.toPx())
                            drawLine(Color.Black, leftMid, rightBottom, strokeWidth = 2.dp.toPx())
                            drawCircle(Color.Black, r, leftMid)
                            drawCircle(Color.Black, r, rightTop)
                            drawCircle(Color.Black, r, rightBottom)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Share to Story", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 9. USERNAME CUSTOMIZATION DIALOG (Liquid Glass)
// =========================================================================
@Composable
fun EditUsernameDialog(
    currentName: String,
    currentTheme: AppTheme,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top avatar icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(currentTheme.accentColor.copy(alpha = 0.2f))
                        .border(1.dp, currentTheme.accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        val strokeW = 2.dp.toPx()
                        drawCircle(
                            color = currentTheme.accentColor,
                            radius = size.width * 0.22f,
                            center = Offset(size.width / 2f, size.height * 0.32f),
                            style = Stroke(width = strokeW)
                        )
                        drawArc(
                            color = currentTheme.accentColor,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.15f, size.height * 0.52f),
                            size = Size(size.width * 0.7f, size.height * 0.44f),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "HYDRATION PROFILE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Personalize your display name across daily tracking, logs, and milestone story cards.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { if (it.length <= 32) textInput = it },
                    singleLine = true,
                    label = { Text("Display Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = currentTheme.accentColor,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = currentTheme.accentColor,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.18f),
                        cursorColor = currentTheme.accentColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Stored privately on your device via Jetpack DataStore",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val trimmed = textInput.trim()
                            if (trimmed.isNotBlank()) {
                                onConfirm(trimmed)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                        shape = RoundedCornerShape(14.dp),
                        enabled = textInput.isNotBlank()
                    ) {
                        Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 10. LEGAL TERMS & PRIVACY POLICY DIALOG (Liquid Glass)
// =========================================================================
@Composable
fun LegalTermsAndPrivacyDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "USER AGREEMENT & PRIVACY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Fluid Tracker Pro • Open Source Release",
                            style = MaterialTheme.typography.labelSmall,
                            color = currentTheme.accentColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawLine(Color.White, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(Color.White, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1
                LegalSectionItem(
                    number = "1",
                    title = "End User Agreement & MIT License",
                    content = "Fluid Tracker Pro is free, open-source software developed by S. M. Mahmud Iqbal. Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files, to deal in the Software without restriction, subject to the MIT License conditions.",
                    accentColor = currentTheme.accentColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2
                LegalSectionItem(
                    number = "2",
                    title = "100% On-Device Privacy Guarantee",
                    content = "Your privacy is absolute. Fluid Tracker Pro does not collect, transmit, sell, or analyze your personal or hydration data. All metrics, custom goals, intake logs, streak records, and profile settings are stored exclusively in your device's local Jetpack DataStore sandbox. No accounts, no cloud servers, zero analytics trackers.",
                    accentColor = currentTheme.accentColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3
                LegalSectionItem(
                    number = "3",
                    title = "Hydration Guidance & Medical Disclaimer",
                    content = "Fluid Tracker Pro is an interactive wellness tool created to promote daily hydration awareness and mindful habits. It does not provide medical diagnoses, treatment advice, or clinical evaluations. Consult a certified healthcare professional for individual medical hydration guidance.",
                    accentColor = currentTheme.accentColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4
                LegalSectionItem(
                    number = "4",
                    title = "Permissions & System Features",
                    content = "Fluid Tracker Pro requires the POST_NOTIFICATIONS permission exclusively for local user-scheduled hydration reminders, and the Accelerometer sensor for real-time liquid tilt simulation. No background tracking or location data is ever requested.",
                    accentColor = currentTheme.accentColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("I Understand & Accept", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LegalSectionItem(
    number: String,
    title: String,
    content: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}
