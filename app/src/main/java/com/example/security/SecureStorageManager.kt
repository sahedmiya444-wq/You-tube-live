package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStorageManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "secure_youtube_stream_prefs"
        private const val KEY_ALIAS = "AutoLiveStudioKeyStoreAlias"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12

        private const val PREF_KEY_SERVER_URL = "pref_server_url"
        private const val PREF_KEY_STREAM_KEY_ENC = "pref_stream_key_enc"
        private const val PREF_KEY_STREAM_KEY_IV = "pref_stream_key_iv"
        const val DEFAULT_SERVER_URL = "rtmps://a.rtmp.youtube.com/live2"
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(parameterSpec)
            return keyGenerator.generateKey()
        }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    fun saveCredentials(serverUrl: String, streamKey: String) {
        val trimmedServer = serverUrl.trim()
        val trimmedKey = streamKey.trim()

        if (trimmedKey.isEmpty()) {
            prefs.edit()
                .putString(PREF_KEY_SERVER_URL, trimmedServer)
                .remove(PREF_KEY_STREAM_KEY_ENC)
                .remove(PREF_KEY_STREAM_KEY_IV)
                .apply()
            return
        }

        try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(trimmedKey.toByteArray(Charsets.UTF_8))

            val encBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

            prefs.edit()
                .putString(PREF_KEY_SERVER_URL, trimmedServer)
                .putString(PREF_KEY_STREAM_KEY_ENC, encBase64)
                .putString(PREF_KEY_STREAM_KEY_IV, ivBase64)
                .apply()
        } catch (_: Exception) {
            // Fallback: don't store key if keystore hardware fails
            prefs.edit().putString(PREF_KEY_SERVER_URL, trimmedServer).apply()
        }
    }

    fun loadCredentials(): Pair<String, String> {
        val serverUrl = prefs.getString(PREF_KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        val encBase64 = prefs.getString(PREF_KEY_STREAM_KEY_ENC, null)
        val ivBase64 = prefs.getString(PREF_KEY_STREAM_KEY_IV, null)

        if (encBase64 == null || ivBase64 == null) {
            return Pair(serverUrl, "")
        }

        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val encBytes = Base64.decode(encBase64, Base64.NO_WRAP)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val decryptedBytes = cipher.doFinal(encBytes)
            val streamKey = String(decryptedBytes, Charsets.UTF_8)
            Pair(serverUrl, streamKey)
        } catch (_: Exception) {
            Pair(serverUrl, "")
        }
    }

    fun clearCredentials() {
        prefs.edit()
            .remove(PREF_KEY_STREAM_KEY_ENC)
            .remove(PREF_KEY_STREAM_KEY_IV)
            .apply()
    }

    fun hasStoredKey(): Boolean {
        return prefs.contains(PREF_KEY_STREAM_KEY_ENC)
    }
}
