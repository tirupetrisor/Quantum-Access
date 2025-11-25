package com.example.quantumaccess.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager securizat pentru preferințe folosind EncryptedSharedPreferences.
 * Gestionează date sensibile (chei, token-uri scurte, flags) criptat pe disk.
 */
@Singleton
class SecurePrefsManager @Inject constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "quantum_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Device ID
    fun getDeviceId(): String {
        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }
        return id
    }

    fun setDeviceId(id: String) {
        prefs.edit().putString(KEY_DEVICE_ID, id).apply()
    }

    // Biometric
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    // Demo Flags
    fun isMockQkdEnabled(): Boolean {
        return prefs.getBoolean(KEY_MOCK_QKD, false) // Default false for production safety
    }

    fun setMockQkdEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MOCK_QKD, enabled).apply()
    }

    fun isMockLocationEnabled(): Boolean {
        return prefs.getBoolean(KEY_MOCK_LOCATION, false)
    }

    fun setMockLocationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MOCK_LOCATION, enabled).apply()
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_MOCK_QKD = "mock_qkd_enabled"
        private const val KEY_MOCK_LOCATION = "mock_location_enabled"
    }
}

