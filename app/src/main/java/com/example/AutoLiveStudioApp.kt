package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class AutoLiveStudioApp : Application() {

    companion object {
        const val CHANNEL_ID_STREAMING = "channel_streaming_service"
        const val CHANNEL_NAME_STREAMING = "Live Stream Service"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_STREAMING,
                CHANNEL_NAME_STREAMING,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live broadcast status, loop counter, and stop controls"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
