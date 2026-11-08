package com.example.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VideoMetadataHelper {

    suspend fun extractMetadata(context: Context, uri: Uri): Result<Pair<VideoItem, Bitmap?>> =
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                // Get filename & size from ContentResolver
                var displayName = "video_${System.currentTimeMillis()}.mp4"
                var sizeBytes = 0L

                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            displayName = it.getString(nameIndex) ?: displayName
                        }
                        if (sizeIndex != -1) {
                            sizeBytes = it.getLong(sizeIndex)
                        }
                    }
                }

                // Retrieve video tracks and dimensions
                retriever.setDataSource(context, uri)

                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L

                val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val width = widthStr?.toIntOrNull() ?: 0
                val height = heightStr?.toIntOrNull() ?: 0

                val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
                val hasAudio = hasAudioStr != null && hasAudioStr.equals("yes", ignoreCase = true)

                val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                    ?: "video/mp4"

                // Extract a frame thumbnail
                val thumbnail: Bitmap? = try {
                    retriever.getFrameAtTime(
                        (durationMs * 500).coerceAtLeast(0L), // Frame at halfway or beginning
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                } catch (_: Exception) {
                    null
                }

                val item = VideoItem(
                    uri = uri,
                    name = displayName,
                    sizeBytes = sizeBytes,
                    durationMs = durationMs,
                    width = width,
                    height = height,
                    hasAudio = hasAudio,
                    mimeType = mimeType
                )

                Result.success(Pair(item, thumbnail))
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }
        }
}
