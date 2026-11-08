package com.example.model

import java.util.Locale

enum class StreamStatus(val label: String) {
    IDLE("Idle"),
    PREPARING("Preparing Media..."),
    CONNECTING("Connecting to YouTube..."),
    LIVE("LIVE"),
    RECONNECTING("Reconnecting..."),
    PAUSED("Paused"),
    STOPPED("Stream Finished"),
    ERROR("Connection Error")
}

data class StreamState(
    val status: StreamStatus = StreamStatus.IDLE,
    val elapsedSeconds: Long = 0L,
    val totalDurationSeconds: Long = 0L,
    val currentBitrateBps: Long = 0L,
    val loopCount: Int = 0,
    val activeVideoTitle: String = "",
    val activeResolution: String = "",
    val activeFps: Int = 30,
    val errorMessage: String? = null,
    val reconnectAttempt: Int = 0
) {
    val isStreaming: Boolean
        get() = status == StreamStatus.LIVE || status == StreamStatus.CONNECTING ||
                status == StreamStatus.RECONNECTING || status == StreamStatus.PAUSED

    val remainingSeconds: Long
        get() {
            if (totalDurationSeconds <= 0) return 0L
            val rem = totalDurationSeconds - elapsedSeconds
            return if (rem > 0) rem else 0L
        }

    val currentBitrateKbps: Long
        get() = currentBitrateBps / 1000

    val formattedElapsed: String
        get() = formatTime(elapsedSeconds)

    val formattedRemaining: String
        get() = if (totalDurationSeconds <= 0) "Continuous" else formatTime(remainingSeconds)

    private fun formatTime(totalSec: Long): String {
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
