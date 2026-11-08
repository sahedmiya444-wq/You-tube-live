# AUTO LIVE STUDIO
### Continuous Video Loop Broadcaster to YouTube Live (Native Android)

**Auto Live Studio** is a native Android application built with Kotlin and Jetpack Compose that decodes any local MP4 video file and streams it continuously in a seamless loop to **YouTube Live** via **RTMPS** with hardware-accelerated MediaCodec encoding and full Android Foreground Service background support.

---

## 🚀 Key Features

1. **Automatic Video Loop Engine**
   - Directly decodes local MP4 videos using Android's hardware `MediaCodec`.
   - Re-encodes video to H.264 (AVC) and audio to AAC-LC at the recommended 2-second keyframe (GOP) interval.
   - Automatically loops the file seamlessly when it reaches the end without terminating the RTMP connection or causing stream buffering on YouTube.
   - Tracks the active loop iteration in real-time (e.g., `Loop #3`).

2. **Full YouTube RTMPS Support**
   - Connects securely to YouTube Live ingestion endpoints (`rtmps://a.rtmp.youtube.com/live2` or backup servers).
   - Real-time connection checker with automatic reconnection retry logic if transient network dropouts occur.

3. **Android Keystore Hardware Security**
   - The user's YouTube Stream Key is encrypted using hardware-backed **AES-256-GCM** inside the **Android Keystore**.
   - Stream keys are never logged, never exposed, and never sent to external third parties.
   - Masked input with one-touch paste and easy credential wipe.

4. **Background Streaming Foreground Service**
   - Streams continuously in the background when navigating to other apps or when the screen is locked/turned off.
   - Manages CPU `PowerManager.PARTIAL_WAKE_LOCK` and high-performance `WifiManager.WifiLock`.
   - Ongoing system notification with live status, loop counter, elapsed/remaining countdown, and one-tap **STOP BROADCAST** control.

5. **Local Storage Access Framework (SAF)**
   - Zero-permission MP4 video selection using Android's native file picker.
   - Extracts duration, resolution, file size, audio tracks, and video thumbnail locally.
   - Integrated **Media3 ExoPlayer** in-app player for previewing the loop before starting the broadcast.

6. **Broadcast Quality & Duration Control**
   - **Resolution Presets:** 480p SD, 720p HD (Recommended), 1080p Full HD, or Match Source Resolution.
   - **Framerate:** 24 FPS (Cinematic) or 30 FPS (Standard Live).
   - **Bitrate:** Auto-calibrated or manual slider from 1,000 kbps to 8,000 kbps.
   - **Duration Presets:** 30 Minutes, 1 Hour, 2 Hours, 3 Hours, Custom Schedule (Hours/Minutes), or Continuous / Infinite.

---

## 🛠️ Architecture & Technologies

- **Language:** Kotlin 2.2
- **UI Framework:** Jetpack Compose with Material Design 3 (Dark Professional Studio Theme)
- **Streaming Core:** `com.github.pedroSG94.RootEncoder` (`RtmpFromFile`) with hardware-accelerated MediaCodec
- **Media Preview:** AndroidX Media3 ExoPlayer
- **Security:** AndroidKeyStore AES/GCM encryption
- **Concurrency & State:** Kotlin Coroutines, StateFlow, SupervisorJob
- **Lifecycle & Background:** Android Foreground Service with `mediaPlayback` and `dataSync` types

---

## 📡 How to Broadcast to YouTube Live

1. Open [YouTube Studio](https://studio.youtube.com/) on your browser or phone.
2. Click **Create** (camera icon) ➔ **Go Live**.
3. In the **Stream** tab, copy your **Stream key**.
4. In **Auto Live Studio**:
   - Tap **Browse Phone Storage** and select your MP4 video file.
   - Paste your YouTube Stream Key into the field.
   - Choose your duration (e.g., 1 Hour, 2 Hours, or Continuous).
   - Tap **START LIVE BROADCAST**.
5. Return to YouTube Studio Live Control Room — within 5–10 seconds, YouTube will detect the incoming stream and display **"Excellent Connection"**!

---

## 🔋 Android Background Streaming Guide

Android device manufacturers (Samsung, Xiaomi, OnePlus, Huawei) enforce aggressive battery optimization policies that can kill long-running background tasks.

For uninterrupted multi-hour streaming:
1. Tap the **Battery Icon** in the top bar of Auto Live Studio.
2. Select **Disable Optimization** / **Open App Settings**.
3. Set Battery usage to **"Unrestricted"** (Samsung) or **"No restrictions"** (Xiaomi/MIUI).
