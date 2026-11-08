package com.example.model

enum class StreamResolution(
    val label: String,
    val width: Int,
    val height: Int,
    val defaultBitrateKbps: Int
) {
    RES_480P("480p SD", 854, 480, 1500),
    RES_720P("720p HD (Recommended)", 1280, 720, 3000),
    RES_1080P("1080p Full HD", 1920, 1080, 5000),
    RES_SOURCE("Match Video Source", 0, 0, 3500)
}

enum class DurationPreset(val label: String, val minutes: Int) {
    MINUTES_30("30 Minutes", 30),
    HOURS_1("1 Hour", 60),
    HOURS_2("2 Hours", 120),
    HOURS_3("3 Hours", 180),
    CUSTOM("Custom", -1),
    INFINITE("Continuous (No Limit)", 0)
}

data class StreamConfig(
    val serverUrl: String = "rtmps://a.rtmp.youtube.com/live2",
    val streamKey: String = "",
    val resolution: StreamResolution = StreamResolution.RES_720P,
    val fps: Int = 30,
    val isAutoBitrate: Boolean = true,
    val customBitrateKbps: Int = 3000,
    val durationPreset: DurationPreset = DurationPreset.HOURS_1,
    val customHours: Int = 1,
    val customMinutes: Int = 0
) {
    val effectiveBitrateKbps: Int
        get() = if (isAutoBitrate) resolution.defaultBitrateKbps else customBitrateKbps

    val totalDurationSeconds: Long
        get() = when (durationPreset) {
            DurationPreset.INFINITE -> 0L
            DurationPreset.CUSTOM -> (customHours * 3600L) + (customMinutes * 60L)
            else -> durationPreset.minutes * 60L
        }

    fun getTargetDimensions(sourceWidth: Int, sourceHeight: Int): Pair<Int, Int> {
        if (resolution == StreamResolution.RES_SOURCE) {
            val w = if (sourceWidth > 0) (sourceWidth / 2) * 2 else 1280
            val h = if (sourceHeight > 0) (sourceHeight / 2) * 2 else 720
            return Pair(w, h)
        }
        return Pair(resolution.width, resolution.height)
    }
}
