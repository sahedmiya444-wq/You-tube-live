package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StreamState
import com.example.model.StreamStatus
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.LiveNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.YouTubeRed

@Composable
fun LiveMonitoringCard(
    state: StreamState,
    modifier: Modifier = Modifier
) {
    // Pulsing live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val statusColor = when (state.status) {
        StreamStatus.LIVE -> LiveNeonGreen
        StreamStatus.CONNECTING, StreamStatus.PREPARING -> WarningAmber
        StreamStatus.RECONNECTING -> Color(0xFFFF9100)
        StreamStatus.PAUSED -> InfoBlue
        StreamStatus.ERROR -> YouTubeRed
        StreamStatus.STOPPED, StreamStatus.IDLE -> TextMuted
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_monitoring_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (state.status == StreamStatus.LIVE) LiveNeonGreen.copy(alpha = 0.5f) else DarkCardBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                            .then(
                                if (state.status == StreamStatus.LIVE || state.status == StreamStatus.CONNECTING) {
                                    Modifier.alpha(pulseAlpha)
                                } else {
                                    Modifier
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.status.label.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                // Loop Counter Badge
                if (state.isStreaming || state.loopCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF003816),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LiveNeonGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Loop,
                                contentDescription = "Loop Count",
                                tint = LiveNeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOOP #${state.loopCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LiveNeonGreen,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Error or Reconnecting Banner
            AnimatedVisibility(visible = state.errorMessage != null) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (state.status == StreamStatus.ERROR) Color(0xFF3B1010) else Color(0xFF332000),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = if (state.status == StreamStatus.ERROR) YouTubeRed else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timers Grid: Elapsed vs Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "ELAPSED TIME",
                    value = state.formattedElapsed,
                    subtitle = "Continuous Live",
                    isTimer = true,
                    modifier = Modifier.weight(1f)
                )

                MetricTile(
                    title = "REMAINING TIME",
                    value = state.formattedRemaining,
                    subtitle = if (state.totalDurationSeconds > 0) "Auto-Stop Target" else "Infinite Mode",
                    isTimer = true,
                    highlight = state.totalDurationSeconds > 0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Telemetry Grid: Bitrate & Resolution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upload Bitrate
                val bitrateMbps = String.format("%.2f", state.currentBitrateKbps / 1000f)
                MetricTile(
                    title = "UPLOAD SPEED",
                    value = if (state.isStreaming && state.currentBitrateKbps > 0) "$bitrateMbps Mbps" else "${state.currentBitrateKbps} kbps",
                    subtitle = if (state.currentBitrateKbps > 2000) "Optimal (H.264)" else if (state.isStreaming) "Uploading..." else "Standby",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )

                // Resolution & FPS
                val resValue = if (state.activeResolution.isNotEmpty()) state.activeResolution else "720p HD"
                MetricTile(
                    title = "BROADCAST FORMAT",
                    value = resValue,
                    subtitle = "${state.activeFps} FPS • AAC Audio",
                    icon = Icons.Default.CellTower,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    isTimer: Boolean = false,
    highlight: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (highlight) LiveNeonGreen else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontFamily = if (isTimer) FontFamily.Monospace else FontFamily.Default
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
