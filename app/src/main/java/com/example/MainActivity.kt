package com.example

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.media.VideoMetadataHelper
import com.example.model.StreamConfig
import com.example.model.VideoItem
import com.example.security.SecureStorageManager
import com.example.service.LiveStreamingService
import com.example.service.StreamingStateManager
import com.example.ui.MainDashboardScreen
import com.example.ui.theme.AutoLiveStudioTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var secureStorage: SecureStorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        secureStorage = SecureStorageManager(this)

        setContent {
            AutoLiveStudioTheme {
                val streamState by StreamingStateManager.state.collectAsStateWithLifecycle()
                val coroutineScope = rememberCoroutineScope()

                // Stream configuration state
                var streamConfig by remember {
                    val (savedUrl, savedKey) = secureStorage.loadCredentials()
                    mutableStateOf(
                        StreamConfig(
                            serverUrl = savedUrl.ifBlank { SecureStorageManager.DEFAULT_SERVER_URL },
                            streamKey = savedKey
                        )
                    )
                }

                // Selected video state
                var selectedVideo by remember { mutableStateOf<VideoItem?>(null) }
                var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }

                // Notification permission launcher for Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(
                            this,
                            "Notification permission recommended for background streaming status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Request notification permission once on startup if Android 13+
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // SAF Video Picker
                val videoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { videoUri ->
                        try {
                            contentResolver.takePersistableUriPermission(
                                videoUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Exception) {
                            // Non-fatal if URI does not support persistable flags
                        }

                        coroutineScope.launch {
                            val result = VideoMetadataHelper.extractMetadata(this@MainActivity, videoUri)
                            result.onSuccess { (item, thumb) ->
                                selectedVideo = item
                                videoThumbnail = thumb
                                Toast.makeText(
                                    this@MainActivity,
                                    "Loaded: ${item.name} (${item.formattedDuration})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.onFailure { err ->
                                Toast.makeText(
                                    this@MainActivity,
                                    "Could not read video metadata: ${err.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                MainDashboardScreen(
                    streamState = streamState,
                    streamConfig = streamConfig,
                    selectedVideo = selectedVideo,
                    videoThumbnail = videoThumbnail,
                    onConfigChange = { newConfig ->
                        streamConfig = newConfig
                        secureStorage.saveCredentials(newConfig.serverUrl, newConfig.streamKey)
                    },
                    onPickVideo = {
                        videoPickerLauncher.launch("video/mp4")
                    },
                    onRemoveVideo = {
                        selectedVideo = null
                        videoThumbnail = null
                    },
                    onStartStream = {
                        val video = selectedVideo
                        if (video == null) {
                            Toast.makeText(this, "Please select an MP4 video", Toast.LENGTH_SHORT).show()
                            return@MainDashboardScreen
                        }
                        if (streamConfig.streamKey.isBlank()) {
                            Toast.makeText(this, "Please enter your YouTube Stream Key", Toast.LENGTH_SHORT).show()
                            return@MainDashboardScreen
                        }

                        val (targetW, targetH) = streamConfig.getTargetDimensions(video.width, video.height)

                        LiveStreamingService.startStreaming(
                            context = this,
                            videoUri = video.uri,
                            serverUrl = streamConfig.serverUrl,
                            streamKey = streamConfig.streamKey,
                            bitrateKbps = streamConfig.effectiveBitrateKbps,
                            fps = streamConfig.fps,
                            width = targetW,
                            height = targetH,
                            durationSeconds = streamConfig.totalDurationSeconds,
                            videoTitle = video.name,
                            hasAudio = video.hasAudio
                        )
                    },
                    onPauseStream = {
                        val pauseIntent = Intent(this, LiveStreamingService::class.java).apply {
                            action = LiveStreamingService.ACTION_PAUSE
                        }
                        startService(pauseIntent)
                    },
                    onResumeStream = {
                        val resumeIntent = Intent(this, LiveStreamingService::class.java).apply {
                            action = LiveStreamingService.ACTION_RESUME
                        }
                        startService(resumeIntent)
                    },
                    onStopStream = {
                        LiveStreamingService.stopStreaming(this)
                    },
                    onClearKey = {
                        secureStorage.clearCredentials()
                        streamConfig = streamConfig.copy(streamKey = "")
                        Toast.makeText(this, "Cleared stored YouTube Stream Key", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
