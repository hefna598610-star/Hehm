package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.TriggerAction
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedAlert
import java.util.Locale

@Composable
fun SettingsSection(
    threshold: Float,
    selectedAction: TriggerAction,
    isAudioEnabled: Boolean,
    isHapticEnabled: Boolean,
    isBackgroundServiceRunning: Boolean,
    onThresholdChange: (Float) -> Unit,
    onSelectAction: (TriggerAction) -> Unit,
    onToggleAudio: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleBackgroundService: () -> Unit,
    onTestPopSound: () -> Unit,
    onCalibrateTare: () -> Unit,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(18.dp)
            .testTag("settings_section_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "إعدادات التشغيل بالخلفية والفرقعة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Background Service Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isBackgroundServiceRunning) CyanPrimary.copy(alpha = 0.12f)
                    else Color(0xFF151C26)
                )
                .border(
                    1.dp,
                    if (isBackgroundServiceRunning) CyanPrimary else Color(0xFF263243),
                    RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isBackgroundServiceRunning) CyanPrimary else AmberWarning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "تشغيل الرصد في الخلفية",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isBackgroundServiceRunning)
                            "الخدمة نشطة الآن: ستصدر الفرقعة والموجات حتى وأنت فاتح تطبيق ثاني!"
                        else
                            "اضغط لتفعيل المراقبة المستمرة خارج التطبيق",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Switch(
                checked = isBackgroundServiceRunning,
                onCheckedChange = { onToggleBackgroundService() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanPrimary,
                    checkedTrackColor = CyanPrimary.copy(alpha = 0.35f)
                ),
                modifier = Modifier.testTag("bg_service_switch")
            )
        }

        // Test Pop sound & waves button
        ElevatedButton(
            onClick = onTestPopSound,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = RedAlert.copy(alpha = 0.2f),
                contentColor = RedAlert
            ),
            modifier = Modifier.fillMaxWidth().testTag("test_pop_sound_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "تجربة صوت الفرقعة والموجات الآن (Test Crackle & Waves)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Threshold Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حد اكتشاف المغناطيس (Threshold)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${String.format(Locale.US, "%.0f", threshold)} µT",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = RedAlert,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Slider(
                value = threshold,
                onValueChange = onThresholdChange,
                valueRange = 60f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = RedAlert,
                    activeTrackColor = RedAlert
                ),
                modifier = Modifier.fillMaxWidth().testTag("threshold_slider")
            )

            // Preset chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip("حساس (80 µT)", 80f, threshold, onThresholdChange)
                PresetChip("افتراضي (120 µT)", 120f, threshold, onThresholdChange)
                PresetChip("قوي (180 µT)", 180f, threshold, onThresholdChange)
            }
        }

        // Action selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "الإجراء التلقائي عند اقتراب المغناطيس:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            TriggerAction.values().forEach { action ->
                val isSelected = selectedAction == action
                val borderColor = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                val bgColor = if (isSelected) CyanPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onSelectAction(action) }
                        .padding(12.dp)
                        .testTag("action_item_${action.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = action.titleAr,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = action.descriptionAr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "موجات اهتزازية ترج التليفون وتروح", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = isHapticEnabled,
                onCheckedChange = { onToggleHaptics() },
                colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary),
                modifier = Modifier.testTag("haptic_switch")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "صوت طلقات الفرقعة (Pop & Crackle)", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = isAudioEnabled,
                onCheckedChange = { onToggleAudio() },
                colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary),
                modifier = Modifier.testTag("audio_switch")
            )
        }

        // Action Buttons: Tare & Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = onCalibrateTare,
                modifier = Modifier.weight(1f).testTag("tare_btn")
            ) {
                Text("معايرة الصفر (Tare)", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = onResetStats,
                modifier = Modifier.weight(1f).testTag("reset_stats_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("إعادة ضبط الإحصائيات", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    value: Float,
    currentThreshold: Float,
    onSelect: (Float) -> Unit
) {
    val isSelected = (currentThreshold - value).let { it in -2f..2f }
    FilterChip(
        selected = isSelected,
        onClick = { onSelect(value) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RedAlert.copy(alpha = 0.2f),
            selectedLabelColor = RedAlert
        )
    )
}
