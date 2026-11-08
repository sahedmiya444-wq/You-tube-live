package com.example

import com.example.model.DurationPreset
import com.example.model.StreamConfig
import com.example.model.StreamResolution
import com.example.model.StreamState
import com.example.model.StreamStatus
import com.example.service.StreamingStateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `stream duration presets calculation test`() {
        val config30m = StreamConfig(durationPreset = DurationPreset.MINUTES_30)
        assertEquals(1800L, config30m.totalDurationSeconds)

        val config1h = StreamConfig(durationPreset = DurationPreset.HOURS_1)
        assertEquals(3600L, config1h.totalDurationSeconds)

        val config2h = StreamConfig(durationPreset = DurationPreset.HOURS_2)
        assertEquals(7200L, config2h.totalDurationSeconds)

        val config3h = StreamConfig(durationPreset = DurationPreset.HOURS_3)
        assertEquals(10800L, config3h.totalDurationSeconds)

        val configCustom = StreamConfig(
            durationPreset = DurationPreset.CUSTOM,
            customHours = 2,
            customMinutes = 30
        )
        assertEquals((2 * 3600L) + (30 * 60L), configCustom.totalDurationSeconds)

        val configInfinite = StreamConfig(durationPreset = DurationPreset.INFINITE)
        assertEquals(0L, configInfinite.totalDurationSeconds)
    }

    @Test
    fun `target dimensions resolution matching test`() {
        val config720p = StreamConfig(resolution = StreamResolution.RES_720P)
        val (w720, h720) = config720p.getTargetDimensions(1920, 1080)
        assertEquals(1280, w720)
        assertEquals(720, h720)

        val config1080p = StreamConfig(resolution = StreamResolution.RES_1080P)
        val (w1080, h1080) = config1080p.getTargetDimensions(1280, 720)
        assertEquals(1920, w1080)
        assertEquals(1080, h1080)

        val configSource = StreamConfig(resolution = StreamResolution.RES_SOURCE)
        val (wSource, hSource) = configSource.getTargetDimensions(1081, 719)
        assertEquals(1080, wSource) // Enforces even number for encoders
        assertEquals(718, hSource)
    }

    @Test
    fun `streaming state manager transitions and loop counting test`() {
        StreamingStateManager.resetToIdle()
        assertEquals(StreamStatus.IDLE, StreamingStateManager.state.value.status)
        assertFalse(StreamingStateManager.state.value.isStreaming)

        StreamingStateManager.setInitialStreaming(
            videoTitle = "loop_video.mp4",
            resolution = "1280x720",
            fps = 30,
            totalDurationSeconds = 3600L
        )

        assertEquals(StreamStatus.PREPARING, StreamingStateManager.state.value.status)
        assertEquals(1, StreamingStateManager.state.value.loopCount)
        assertEquals(3600L, StreamingStateManager.state.value.totalDurationSeconds)

        StreamingStateManager.updateStatus(StreamStatus.LIVE)
        assertEquals(StreamStatus.LIVE, StreamingStateManager.state.value.status)
        assertTrue(StreamingStateManager.state.value.isStreaming)

        // Increment loop on video end
        StreamingStateManager.incrementLoop()
        assertEquals(2, StreamingStateManager.state.value.loopCount)

        StreamingStateManager.tickElapsed()
        assertEquals(1L, StreamingStateManager.state.value.elapsedSeconds)
        assertEquals(3599L, StreamingStateManager.state.value.remainingSeconds)

        StreamingStateManager.updateBitrate(3500000L)
        assertEquals(3500L, StreamingStateManager.state.value.currentBitrateKbps)

        StreamingStateManager.updateStatus(StreamStatus.STOPPED)
        assertFalse(StreamingStateManager.state.value.isStreaming)
    }
}
