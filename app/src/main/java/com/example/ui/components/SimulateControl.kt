package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedAlert
import java.util.Locale

@Composable
fun SimulateControl(
    isSimulating: Boolean,
    currentMagnitude: Float,
    onToggleSimulation: () -> Unit,
    onSimulateStrength: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(16.dp)
            .testTag("simulation_control_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = if (isSimulating) AmberWarning else CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "محاكي المغناطيس الافتراضي",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isSimulating) "وضع المحاكاة نشط" else "المستشعر الحقيقي نشط",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Switch(
                checked = isSimulating,
                onCheckedChange = { onToggleSimulation() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanPrimary,
                    checkedTrackColor = CyanPrimary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("simulation_switch")
            )
        }

        if (isSimulating) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "اسحب الشريط لاختبار تقريب المغناطيس وتجربة الإخفاء: ${String.format(Locale.US, "%.0f", currentMagnitude)} µT",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Slider(
                value = currentMagnitude.coerceIn(30f, 240f),
                onValueChange = { onSimulateStrength(it) },
                valueRange = 30f..240f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanPrimary,
                    activeTrackColor = CyanPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("simulation_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onSimulateStrength(45f) },
                    modifier = Modifier.weight(1f).testTag("sim_normal_btn")
                ) {
                    Text("بيئة عادية", style = MaterialTheme.typography.labelSmall)
                }

                FilledTonalButton(
                    onClick = { onSimulateStrength(90f) },
                    modifier = Modifier.weight(1f).testTag("sim_elevated_btn")
                ) {
                    Text("جهاز إلكتروني", style = MaterialTheme.typography.labelSmall)
                }

                ElevatedButton(
                    onClick = { onSimulateStrength(160f) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = RedAlert.copy(alpha = 0.25f),
                        contentColor = RedAlert
                    ),
                    modifier = Modifier.weight(1.2f).testTag("sim_trigger_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تقريب مغناطيس!", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
