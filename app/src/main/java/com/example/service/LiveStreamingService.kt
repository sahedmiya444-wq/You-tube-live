package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.AutoLiveStudioApp
import com.example.MainActivity
import com.example.R
import com.example.model.StreamStatus
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.decoder.AudioDecoderInterface
import com.pedro.encoder.input.decoder.VideoDecoderInterface
import com.pedro.library.rtmp.RtmpFromFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiveStreamingService : Service(), ConnectChecker, VideoDecoderInterface, AudioDecoderInterface {

    companion object {
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.action.START_STREAM"
        const val ACTION_STOP = "com.example.action.STOP_STREAM"
        const val ACTION_PAUSE = "com.example.action.PAUSE_STREAM"
        const val ACTION_RESUME = "com.example.action.RESUME_STREAM"

        const val EXTRA_VIDEO_URI = "extra_video_uri"
        const val EXTRA_SERVER_URL = "extra_server_url"
        const val EXTRA_STREAM_KEY = "extra_stream_key"
        const val EXTRA_BITRATE_KBPS = "extra_bitrate_kbps"
        const val EXTRA_FPS = "extra_fps"
        const val EXTRA_WIDTH = "extra_width"
        const val EXTRA_HEIGHT = "extra_height"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        const val EXTRA_VIDEO_TITLE = "extra_video_title"
        const val EXTRA_HAS_AUDIO = "extra_has_audio"

        fun startStreaming(
            context: Context,
            videoUri: Uri,
            serverUrl: String,
            streamKey: String,
            bitrateKbps: Int,
            fps: Int,
            width: Int,
            height: Int,
            durationSeconds: Long,
            videoTitle: String,
            hasAudio: Boolean
        ) {
            val intent = Intent(context, LiveStreamingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_VIDEO_URI, videoUri.toString())
                putExtra(EXTRA_SERVER_URL, serverUrl)
                putExtra(EXTRA_STREAM_KEY, streamKey)
                putExtra(EXTRA_BITRATE_KBPS, bitrateKbps)
                putExtra(EXTRA_FPS, fps)
                putExtra(EXTRA_WIDTH, width)
                putExtra(EXTRA_HEIGHT, height)
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
                putExtra(EXTRA_VIDEO_TITLE, videoTitle)
                putExtra(EXTRA_HAS_AUDIO, hasAudio)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopStreaming(context: Context) {
            val intent = Intent(context, LiveStreamingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    private var rtmpFromFile: RtmpFromFile? = null
    private var fullRtmpEndpoint: String = ""
    private var currentVideoUri: Uri? = null
    private var currentVideoTitle: String = ""
    private var targetBitrateKbps: Int = 3000
    private var targetFps: Int = 30
    private var targetWidth: Int = 0
    private var targetHeight: Int = 0
    private var maxDurationSeconds: Long = 0L
    private var videoHasAudio: Boolean = true

    private var isUserStopping: Boolean = false
    private var reconnectCount: Int = 0
    private val maxReconnectAttempts = 3

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        acquireLocks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_STOP -> handleStop("Stopped by user")
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        val uriStr = intent.getStringExtra(EXTRA_VIDEO_URI) ?: return
        val serverUrl = intent.getStringExtra(EXTRA_SERVER_URL) ?: ""
        val streamKey = intent.getStringExtra(EXTRA_STREAM_KEY) ?: ""

        if (serverUrl.isBlank() || streamKey.isBlank()) {
            StreamingStateManager.updateStatus(
                StreamStatus.ERROR,
                "Invalid YouTube Server URL or Stream Key"
            )
            stopSelf()
            return
        }

        currentVideoUri = Uri.parse(uriStr)
        currentVideoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE) ?: "Live Broadcast"
        targetBitrateKbps = intent.getIntExtra(EXTRA_BITRATE_KBPS, 3000)
        targetFps = intent.getIntExtra(EXTRA_FPS, 30)
        targetWidth = intent.getIntExtra(EXTRA_WIDTH, 0)
        targetHeight = intent.getIntExtra(EXTRA_HEIGHT, 0)
        maxDurationSeconds = intent.getLongExtra(EXTRA_DURATION_SECONDS, 0L)
        videoHasAudio = intent.getBooleanExtra(EXTRA_HAS_AUDIO, true)

        val cleanServer = serverUrl.trim().removeSuffix("/")
        val cleanKey = streamKey.trim()
        fullRtmpEndpoint = "$cleanServer/$cleanKey"

        isUserStopping = false
        reconnectCount = 0

        // Start Foreground immediately with notification
        val notification = createNotification("Preparing live broadcast...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val resText = if (targetWidth > 0 && targetHeight > 0) "${targetWidth}x${targetHeight}" else "Source Resolution"
        StreamingStateManager.setInitialStreaming(
            videoTitle = currentVideoTitle,
            resolution = resText,
            fps = targetFps,
            totalDurationSeconds = maxDurationSeconds
        )

        // Launch preparation and streaming on background thread
        serviceScope.launch {
            startBroadcastPipeline()
        }
    }

    private suspend fun startBroadcastPipeline() = withContext(Dispatchers.Default) {
        val context = applicationContext
        val uri = currentVideoUri ?: return@withContext

        try {
            // Clean up any existing instance
            rtmpFromFile?.stopStream()
            rtmpFromFile = null

            StreamingStateManager.updateStatus(StreamStatus.PREPARING)
            updateNotification("Preparing hardware encoders...")

            val rtmp = RtmpFromFile(context, this@LiveStreamingService, this@LiveStreamingService, this@LiveStreamingService)
            rtmpFromFile = rtmp

            // Configure automatic seamless loop
            rtmp.setLoopMode(true)

            // Prepare Video
            val bitrateBps = targetBitrateKbps * 1000
            val videoPrepared = if (targetWidth > 0 && targetHeight > 0) {
                rtmp.prepareVideo(context, uri, bitrateBps, targetFps, targetWidth, targetHeight)
            } else {
                rtmp.prepareVideo(context, uri, bitrateBps, targetFps)
            }

            if (!videoPrepared) {
                withContext(Dispatchers.Main) {
                    StreamingStateManager.updateStatus(
                        StreamStatus.ERROR,
                        "Hardware video encoder failed to prepare for this video format."
                    )
                    handleStop("Video encoder initialization failed")
                }
                return@withContext
            }

            // Prepare Audio (if video has audio)
            if (videoHasAudio) {
                val audioPrepared = rtmp.prepareAudio(context, uri)
                if (!audioPrepared) {
                    // Non-fatal: notify audio couldn't be encoded, but video can proceed
                    android.util.Log.w("LiveStreamingService", "Audio track could not be prepared - proceeding video-only")
                }
            }

            StreamingStateManager.updateStatus(StreamStatus.CONNECTING)
            updateNotification("Connecting to YouTube Live...")

            // Start RTMPS broadcast to YouTube
            rtmp.startStream(fullRtmpEndpoint)

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                StreamingStateManager.updateStatus(
                    StreamStatus.ERROR,
                    "Failed to start broadcast: ${e.localizedMessage ?: "Unknown error"}"
                )
                handleStop("Stream initialization exception: ${e.message}")
            }
        }
    }

    private fun startDurationTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                StreamingStateManager.tickElapsed()

                val state = StreamingStateManager.state.value
                if (maxDurationSeconds > 0 && state.elapsedSeconds >= maxDurationSeconds) {
                    handleStop("Selected duration of $maxDurationSeconds seconds completed")
                    break
                }

                // Update notification text every 2 seconds
                if (state.elapsedSeconds % 2L == 0L) {
                    val notifText = buildString {
                        append("LIVE • Loop #")
                        append(state.loopCount)
                        append(" • ")
                        append(state.formattedElapsed)
                        if (maxDurationSeconds > 0) {
                            append(" (Left: ")
                            append(state.formattedRemaining)
                            append(")")
                        }
                    }
                    updateNotification(notifText)
                }
            }
        }
    }

    private fun handlePause() {
        // Mute or pause audio if possible, update status
        StreamingStateManager.setPaused(true)
        updateNotification("LIVE BROADCAST PAUSED")
    }

    private fun handleResume() {
        StreamingStateManager.setPaused(false)
        updateNotification("LIVE BROADCAST RESUMED")
    }

    private fun handleStop(reason: String) {
        isUserStopping = true
        timerJob?.cancel()
        timerJob = null

        try {
            rtmpFromFile?.stopStream()
        } catch (_: Exception) {
        }
        rtmpFromFile = null

        releaseLocks()

        StreamingStateManager.updateStatus(
            StreamStatus.STOPPED,
            if (reason.contains("completed", ignoreCase = true)) "Broadcast completed scheduled duration." else null
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // --- ConnectChecker callbacks ---

    override fun onConnectionStarted(url: String) {
        StreamingStateManager.updateStatus(StreamStatus.CONNECTING)
        updateNotification("Connecting to YouTube Live...")
    }

    override fun onConnectionSuccess() {
        reconnectCount = 0
        StreamingStateManager.updateStatus(StreamStatus.LIVE)
        updateNotification("LIVE • Broadcasting to YouTube")
        startDurationTimer()
    }

    override fun onConnectionFailed(reason: String) {
        if (isUserStopping) return

        if (reconnectCount < maxReconnectAttempts) {
            reconnectCount++
            val retryMsg = "Connection failed ($reason). Reconnecting (Attempt $reconnectCount of $maxReconnectAttempts)..."
            StreamingStateManager.setReconnecting(reconnectCount, retryMsg)
            updateNotification(retryMsg)

            serviceScope.launch {
                delay(3000)
                if (isUserStopping) return@launch
                try {
                    rtmpFromFile?.startStream(fullRtmpEndpoint)
                } catch (e: Exception) {
                    StreamingStateManager.updateStatus(StreamStatus.ERROR, "Reconnection failed: ${e.message}")
                }
            }
        } else {
            val errorMsg = "Could not connect to YouTube ($reason). Check Stream Key and Internet connection."
            StreamingStateManager.updateStatus(StreamStatus.ERROR, errorMsg)
            handleStop(errorMsg)
        }
    }

    override fun onNewBitrate(bitrate: Long) {
        StreamingStateManager.updateBitrate(bitrate)
    }

    override fun onDisconnect() {
        if (isUserStopping) return

        if (reconnectCount < maxReconnectAttempts) {
            reconnectCount++
            val retryMsg = "Disconnected from YouTube. Reconnecting ($reconnectCount/$maxReconnectAttempts)..."
            StreamingStateManager.setReconnecting(reconnectCount, retryMsg)
            updateNotification(retryMsg)

            serviceScope.launch {
                delay(3000)
                if (isUserStopping) return@launch
                try {
                    rtmpFromFile?.startStream(fullRtmpEndpoint)
                } catch (e: Exception) {
                    StreamingStateManager.updateStatus(StreamStatus.ERROR, "Reconnection failed: ${e.message}")
                }
            }
        } else {
            handleStop("Disconnected from server")
        }
    }

    override fun onAuthError() {
        StreamingStateManager.updateStatus(
            StreamStatus.ERROR,
            "YouTube Authentication Error. Verify your YouTube Stream Key is valid and active."
        )
        handleStop("YouTube Authentication Failed")
    }

    override fun onAuthSuccess() {
        // Authenticated successfully
    }

    // --- VideoDecoderInterface callback ---

    override fun onVideoDecoderFinished() {
        // Video file reached the end! Increment loop count!
        StreamingStateManager.incrementLoop()
        android.util.Log.d("LiveStreamingService", "Video completed 1 loop. Incrementing loop count and repeating.")
    }

    // --- AudioDecoderInterface callback ---

    override fun onAudioDecoderFinished() {
        // Audio track finished for current iteration
    }

    // --- Notification & Lock Helpers ---

    private fun createNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LiveStreamingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AutoLiveStudioApp.CHANNEL_ID_STREAMING)
            .setContentTitle("🔴 Auto Live Studio: YouTube Broadcast")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP LIVE", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun acquireLocks() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AutoLiveStudio::StreamWakeLock"
        ).apply {
            acquire(12 * 60 * 60 * 1000L) // 12 hours max safety limit
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(
            WifiManager.WIFI_MODE_FULL_HIGH_PERF,
            "AutoLiveStudio::StreamWifiLock"
        ).apply {
            acquire()
        }
    }

    private fun releaseLocks() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (_: Exception) {
        }
        wakeLock = null

        try {
            if (wifiLock?.isHeld == true) wifiLock?.release()
        } catch (_: Exception) {
        }
        wifiLock = null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        handleStop("Service destroyed")
        super.onDestroy()
    }
}
