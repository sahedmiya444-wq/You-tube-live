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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StreamConfig
import com.example.model.StreamResolution
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.LiveNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QualitySettingsCard(
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
                        .background(Color(0xFF142436), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Encoder Settings",
                        tint = InfoBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ENCODER & BITRATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = InfoBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Hardware Video Compression",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resolution selection
            Text(
                text = "Target Resolution",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StreamResolution.values().forEach { res ->
                    FilterChip(
                        selected = config.resolution == res,
                        onClick = { onConfigChange(config.copy(resolution = res)) },
                        enabled = !isStreaming,
                        label = { Text(res.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF142436),
                            selectedLabelColor = InfoBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Frame Rate
            Text(
                text = "Frame Rate (FPS)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(24, 30).forEach { fpsValue ->
                    FilterChip(
                        selected = config.fps == fpsValue,
                        onClick = { onConfigChange(config.copy(fps = fpsValue)) },
                        enabled = !isStreaming,
                        label = { Text("$fpsValue FPS ${if (fpsValue == 30) "(Standard)" else "(Film)"}", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF142436),
                            selectedLabelColor = InfoBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bitrate mode: Auto vs Manual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Auto Bitrate Optimization",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (config.isAutoBitrate) "Dynamically calibrated for ${config.resolution.label}" else "Manual bitrate override",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = config.isAutoBitrate,
                    onCheckedChange = { onConfigChange(config.copy(isAutoBitrate = it)) },
                    enabled = !isStreaming,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = LiveNeonGreen,
                        checkedTrackColor = Color(0xFF004D22)
                    )
                )
            }

            if (!config.isAutoBitrate) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Target Bitrate", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "${config.customBitrateKbps} kbps (${String.format("%.1f", config.customBitrateKbps / 1000f)} Mbps)",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiveNeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = config.customBitrateKbps.toFloat(),
                    onValueChange = { onConfigChange(config.copy(customBitrateKbps = it.toInt())) },
                    valueRange = 1000f..8000f,
                    steps = 13,
                    enabled = !isStreaming,
                    colors = SliderDefaults.colors(
                        thumbColor = LiveNeonGreen,
                        activeTrackColor = LiveNeonGreen,
                        inactiveTrackColor = DarkCardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // YouTube Codec Specs Banner
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
                        text = "YouTube Live Profile: H.264 (AVC) • AAC-LC Audio • 2s Keyframe Interval",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
