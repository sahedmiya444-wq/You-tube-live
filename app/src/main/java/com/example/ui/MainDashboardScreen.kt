package com.example.ui

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.StreamConfig
import com.example.model.StreamState
import com.example.model.StreamStatus
import com.example.model.VideoItem
import com.example.ui.components.BatteryOptimizationDialog
import com.example.ui.components.DurationPickerCard
import com.example.ui.components.LiveMonitoringCard
import com.example.ui.components.QualitySettingsCard
import com.example.ui.components.StopConfirmationDialog
import com.example.ui.components.VideoPickerCard
import com.example.ui.components.YouTubeConfigCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.YouTubeRed

@Composable
fun MainDashboardScreen(
    streamState: StreamState,
    streamConfig: StreamConfig,
    selectedVideo: VideoItem?,
    videoThumbnail: Bitmap?,
    onConfigChange: (StreamConfig) -> Unit,
    onPickVideo: () -> Unit,
    onRemoveVideo: () -> Unit,
    onStartStream: () -> Unit,
    onPauseStream: () -> Unit,
    onResumeStream: () -> Unit,
    onStopStream: () -> Unit,
    onClearKey: () -> Unit
) {
    val context = LocalContext.current
    var showStopDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    val isLiveOrConnecting = streamState.isStreaming
    val canStart = selectedVideo != null && streamConfig.streamKey.isNotBlank() && !isLiveOrConnecting

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Sticky Floating Action Bar
            Surface(
                color = DarkSurface,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2230)),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isLiveOrConnecting) {
                            Button(
                                onClick = {
                                    if (canStart) {
                                        onStartStream()
                                    } else {
                                        val hint = if (selectedVideo == null) {
                                            "Please select an MP4 video from phone storage first."
                                        } else {
                                            "Please enter your YouTube Stream Key."
                                        }
                                        Toast.makeText(context, hint, Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = canStart,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LiveNeonGreen,
                                    contentColor = Color(0xFF003816),
                                    disabledContainerColor = DarkSurfaceVariant,
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("start_live_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canStart) "START LIVE BROADCAST" else if (selectedVideo == null) "SELECT VIDEO TO BROADCAST" else "ENTER STREAM KEY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else {
                            // When actively streaming
                            val isPaused = streamState.status == StreamStatus.PAUSED

                            OutlinedButton(
                                onClick = if (isPaused) onResumeStream else onPauseStream,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("pause_resume_button")
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPaused) "RESUME" else "PAUSE",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { showStopDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YouTubeRed,
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(54.dp)
                                    .testTag("stop_live_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STOP BROADCAST",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Top App Bar Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AUTO LIVE STUDIO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Native YouTube Loop Broadcaster",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LiveNeonGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showBatteryDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = "Battery info",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Live Monitoring Telemetry Card
            item {
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    LiveMonitoringCard(state = streamState)
                }
            }

            // Source Video Selection Card
            item {
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    VideoPickerCard(
                        selectedVideo = selectedVideo,
                        thumbnail = videoThumbnail,
                        isStreaming = isLiveOrConnecting,
                        onPickVideo = onPickVideo,
                        onRemoveVideo = onRemoveVideo
                    )
                }
            }

            // YouTube RTMPS Settings Card
            item {
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    YouTubeConfigCard(
                        serverUrl = streamConfig.serverUrl,
                        streamKey = streamConfig.streamKey,
                        isStreaming = isLiveOrConnecting,
                        onServerUrlChange = { onConfigChange(streamConfig.copy(serverUrl = it)) },
                        onStreamKeyChange = { onConfigChange(streamConfig.copy(streamKey = it)) },
                        onClearKey = onClearKey
                    )
                }
            }

            // Duration Schedule Card
            item {
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    DurationPickerCard(
                        config = streamConfig,
                        isStreaming = isLiveOrConnecting,
                        onConfigChange = onConfigChange
                    )
                }
            }

            // Encoder Quality Settings Card
            item {
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    QualitySettingsCard(
                        config = streamConfig,
                        isStreaming = isLiveOrConnecting,
                        onConfigChange = onConfigChange
                    )
                }
            }

            // Extra bottom spacing so content doesn't get covered by sticky bottom bar
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showStopDialog) {
        StopConfirmationDialog(
            onConfirm = {
                showStopDialog = false
                onStopStream()
            },
            onDismiss = { showStopDialog = false }
        )
    }

    if (showBatteryDialog) {
        BatteryOptimizationDialog(
            onDismiss = { showBatteryDialog = false }
        )
    }
}
