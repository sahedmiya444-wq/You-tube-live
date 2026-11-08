package com.example.model

import android.net.Uri
import java.util.Locale

data class VideoItem(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val hasAudio: Boolean,
    val mimeType: String = "video/mp4"
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "00:00"
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "0 MB"
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1000) {
                val gb = mb / 1024.0
                String.format(Locale.US, "%.2f GB", gb)
            } else {
                String.format(Locale.US, "%.1f MB", mb)
            }
        }

    val resolutionLabel: String
        get() = if (width > 0 && height > 0) "${width}x${height}" else "Unknown"
}
