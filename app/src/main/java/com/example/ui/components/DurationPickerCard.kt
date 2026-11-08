package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DurationPreset
import com.example.model.StreamConfig
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DurationPickerCard(
    config: StreamConfig,
    isStreaming: Boolean,
    onConfigChange: (StreamConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF2C2412), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = "Duration Schedule",
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "LOOP BROADCAST DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Auto-Stop Countdown Schedule",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DurationPreset.values().forEach { preset ->
                    FilterChip(
                        selected = config.durationPreset == preset,
                        onClick = { onConfigChange(config.copy(durationPreset = preset)) },
                        enabled = !isStreaming,
                        label = { Text(preset.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF332915),
                            selectedLabelColor = WarningAmber
                        )
                    )
                }
            }

            if (config.durationPreset == DurationPreset.CUSTOM) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hours: ${config.customHours}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Minutes: ${config.customMinutes}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Hours Slider (0 to 12)
                        Text(
                            text = "Hours (${config.customHours}h)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Slider(
                            value = config.customHours.toFloat(),
                            onValueChange = { onConfigChange(config.copy(customHours = it.toInt())) },
                            valueRange = 0f..12f,
                            steps = 11,
                            enabled = !isStreaming,
                            colors = SliderDefaults.colors(
                                thumbColor = WarningAmber,
                                activeTrackColor = WarningAmber
                            )
                        )

                        // Minutes Slider (0 to 55)
                        Text(
                            text = "Minutes (${config.customMinutes}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Slider(
                            value = config.customMinutes.toFloat(),
                            onValueChange = { onConfigChange(config.copy(customMinutes = it.toInt())) },
                            valueRange = 0f..55f,
                            steps = 10,
                            enabled = !isStreaming,
                            colors = SliderDefaults.colors(
                                thumbColor = WarningAmber,
                                activeTrackColor = WarningAmber
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explanation
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (config.durationPreset == DurationPreset.INFINITE) {
                            "Broadcaster will continuously loop without limit until you tap 'Stop Live'."
                        } else {
                            val totalMin = when (config.durationPreset) {
                                DurationPreset.CUSTOM -> (config.customHours * 60) + config.customMinutes
                                else -> config.durationPreset.minutes
                            }
                            "Broadcaster will continuously loop video for $totalMin minutes, then automatically finalize the YouTube stream."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
