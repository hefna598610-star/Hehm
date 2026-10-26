package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AxisBreakdown
import com.example.ui.components.EmfGauge
import com.example.ui.components.RadarSweep
import com.example.ui.components.SettingsSection
import com.example.ui.components.ShockwaveRipples
import com.example.ui.components.SimulateControl
import com.example.ui.components.WaveformChart
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedAlert
import com.example.viewmodel.MagnetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MagnetViewModel,
    onMoveToBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isRadarView by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Android 13+ Notification Permission Launcher for Foreground Service
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleBackgroundService()
        }
    }

    val requestBackgroundToggle = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !uiState.isBackgroundServiceRunning) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.toggleBackgroundService()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MagnoScan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "كاشف المجال وموجات الفرقعة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Quick Status Pill
                    if (uiState.isBackgroundServiceRunning) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "الخلفية نشطة",
                                color = CyanPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (uiState.isTriggerActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RedAlert)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "فرقعة!",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding().testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "العداد") },
                    label = { Text("العداد والرادار") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_gauge")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "المخطط") },
                    label = { Text("المخطط الحي") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_chart")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "الإعدادات") },
                    label = { Text("الخلفية والفرقعة") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Background Monitor Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (uiState.isBackgroundServiceRunning) CyanPrimary.copy(alpha = 0.15f)
                        else Color(0xFF141A23)
                    )
                    .border(
                        1.dp,
                        if (uiState.isBackgroundServiceRunning) CyanPrimary.copy(alpha = 0.6f)
                        else Color(0xFF263243),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { requestBackgroundToggle() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (uiState.isBackgroundServiceRunning) CyanPrimary else AmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (uiState.isBackgroundServiceRunning)
                                "مراقبة الخلفية نشطة (جاهز للفرقعة خارج التطبيق)"
                            else
                                "تشغيل التطبيق في الخلفية",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (uiState.isBackgroundServiceRunning)
                                "حتى لو فتحت تطبيقاً آخر، سيصدر صوت الفرقعة والموجات!"
                            else
                                "اضغط لتشغيل الخدمة والاستمرار خارج التطبيق",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Switch(
                    checked = uiState.isBackgroundServiceRunning,
                    onCheckedChange = { requestBackgroundToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary),
                    modifier = Modifier.testTag("quick_bg_switch")
                )
            }

            // Sensor Status Alert banner if missing physical sensor
            if (!uiState.hasHardwareSensor && !uiState.isSimulating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberWarning.copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "هذا الجهاز لا يحتوي على مستشعر مغناطيسي حقيقي. استخدم وضع المحاكاة لاختبار صوت الفرقعة والموجات.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Gauge & Radar view selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !isRadarView,
                            onClick = { isRadarView = false },
                            label = { Text("العداد الدائري") },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            ),
                            modifier = Modifier.testTag("view_gauge_chip")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        FilterChip(
                            selected = isRadarView,
                            onClick = { isRadarView = true },
                            label = { Text("شاشة الرادار") },
                            leadingIcon = {
                                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            ),
                            modifier = Modifier.testTag("view_radar_chip")
                        )
                    }

                    // Main visualization with shockwave ripple overlay!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Shockwave ripple effect that pulses and fades away ("موجات كده اهوت وتروح")
                        ShockwaveRipples(isTriggered = uiState.isTriggerActive)

                        if (!isRadarView) {
                            EmfGauge(
                                magnitude = uiState.currentReading.magnitude,
                                threshold = uiState.threshold,
                                status = uiState.status
                            )
                        } else {
                            RadarSweep(
                                magnitude = uiState.currentReading.magnitude,
                                threshold = uiState.threshold,
                                status = uiState.status
                            )
                        }
                    }

                    // Quick Test Pop & Waves button
                    ElevatedButton(
                        onClick = { viewModel.testFirecrackerPop() },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = RedAlert.copy(alpha = 0.25f),
                            contentColor = RedAlert
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("gauge_test_pop_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تجربة صوت الفرقعة والموجات الآن 💥",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Vector Axes Breakdown (X, Y, Z)
                    AxisBreakdown(
                        x = uiState.currentReading.x,
                        y = uiState.currentReading.y,
                        z = uiState.currentReading.z
                    )

                    // Quick Simulation Bar
                    SimulateControl(
                        isSimulating = uiState.isSimulating,
                        currentMagnitude = uiState.currentReading.magnitude,
                        onToggleSimulation = { viewModel.toggleSimulation() },
                        onSimulateStrength = { viewModel.setSimulatedMagnetStrength(it) }
                    )
                }

                1 -> {
                    // Waveform Chart & Stats
                    WaveformChart(
                        points = uiState.historyPoints,
                        threshold = uiState.threshold,
                        peak = uiState.peakMagnitude,
                        min = uiState.minMagnitude,
                        avg = uiState.avgMagnitude
                    )

                    // Trigger statistics card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "سجل الفرقعات والتنبيهات",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "عدد مرات رصد المغناطيس والفرقعة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Text(
                            text = "${uiState.triggerCount} مرة",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (uiState.triggerCount > 0) RedAlert else CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    SimulateControl(
                        isSimulating = uiState.isSimulating,
                        currentMagnitude = uiState.currentReading.magnitude,
                        onToggleSimulation = { viewModel.toggleSimulation() },
                        onSimulateStrength = { viewModel.setSimulatedMagnetStrength(it) }
                    )
                }

                2 -> {
                    // Settings Section
                    SettingsSection(
                        threshold = uiState.threshold,
                        selectedAction = uiState.selectedAction,
                        isAudioEnabled = uiState.isAudioEnabled,
                        isHapticEnabled = uiState.isHapticEnabled,
                        isBackgroundServiceRunning = uiState.isBackgroundServiceRunning,
                        onThresholdChange = { viewModel.setThreshold(it) },
                        onSelectAction = { viewModel.selectTriggerAction(it) },
                        onToggleAudio = { viewModel.toggleAudio() },
                        onToggleHaptics = { viewModel.toggleHaptics() },
                        onToggleBackgroundService = { requestBackgroundToggle() },
                        onTestPopSound = { viewModel.testFirecrackerPop() },
                        onCalibrateTare = { viewModel.calibrateTare() },
                        onResetStats = { viewModel.resetStats() }
                    )

                    SimulateControl(
                        isSimulating = uiState.isSimulating,
                        currentMagnitude = uiState.currentReading.magnitude,
                        onToggleSimulation = { viewModel.toggleSimulation() },
                        onSimulateStrength = { viewModel.setSimulatedMagnetStrength(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
