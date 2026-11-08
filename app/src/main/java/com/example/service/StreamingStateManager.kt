package com.example.service

import com.example.model.StreamState
import com.example.model.StreamStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object StreamingStateManager {

    private val _state = MutableStateFlow(StreamState())
    val state: StateFlow<StreamState> = _state.asStateFlow()

    fun updateStatus(status: StreamStatus, errorMessage: String? = null) {
        _state.update {
            it.copy(
                status = status,
                errorMessage = errorMessage ?: if (status != StreamStatus.ERROR) null else it.errorMessage
            )
        }
    }

    fun setInitialStreaming(
        videoTitle: String,
        resolution: String,
        fps: Int,
        totalDurationSeconds: Long
    ) {
        _state.value = StreamState(
            status = StreamStatus.PREPARING,
            activeVideoTitle = videoTitle,
            activeResolution = resolution,
            activeFps = fps,
            totalDurationSeconds = totalDurationSeconds,
            elapsedSeconds = 0L,
            loopCount = 1,
            errorMessage = null
        )
    }

    fun updateBitrate(bitrateBps: Long) {
        _state.update { it.copy(currentBitrateBps = bitrateBps) }
    }

    fun incrementLoop() {
        _state.update { it.copy(loopCount = it.loopCount + 1) }
    }

    fun tickElapsed() {
        _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
    }

    fun setReconnecting(attempt: Int, message: String) {
        _state.update {
            it.copy(
                status = StreamStatus.RECONNECTING,
                reconnectAttempt = attempt,
                errorMessage = message
            )
        }
    }

    fun setPaused(paused: Boolean) {
        _state.update {
            if (paused) {
                it.copy(status = StreamStatus.PAUSED)
            } else {
                it.copy(status = StreamStatus.LIVE)
            }
        }
    }

    fun resetToIdle() {
        _state.value = StreamState(status = StreamStatus.IDLE)
    }
}
