package com.example.quantumaccess.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.GeneralSecurityException
import java.util.UUID
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager securizat pentru preferințe folosind EncryptedSharedPreferences.
 * Gestionează date sensibile (chei, token-uri scurte, flags) criptat pe disk.
 */
@Singleton
class SecurePrefsManager @Inject constructor(context: Context) {

    private val prefs: SharedPreferences = initEncryptedPrefs(context)

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

    private fun initEncryptedPrefs(context: Context): SharedPreferences {
        return runCatching { buildEncryptedPrefs(context) }
            .getOrElse { error ->
                if (error.isRecoverableCryptoIssue()) {
                    Log.w(TAG, "Encrypted prefs corrupted. Resetting secure storage.", error)
                    clearSecureStorage(context)
                    buildEncryptedPrefs(context)
                } else {
                    throw error
                }
            }
    }

    private fun buildEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun clearSecureStorage(context: Context) {
        context.deleteSharedPreferences(PREFS_FILE)
        SECURITY_PREFS_FILES.forEach { name ->
            context.deleteSharedPreferences(name)
        }

        val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        sharedPrefsDir.listFiles()
            ?.filter { it.name.contains("androidx.security.crypto") }
            ?.forEach { it.delete() }
    }

    private fun Throwable.isRecoverableCryptoIssue(): Boolean {
        return this is AEADBadTagException ||
            this.cause is AEADBadTagException ||
            this is GeneralSecurityException
    }

    companion object {
        private const val TAG = "SecurePrefsManager"
        private const val PREFS_FILE = "quantum_secure_prefs"
        private val SECURITY_PREFS_FILES = listOf(
            "__androidx_security_crypto_master_key__",
            "__androidx_security_crypto_encrypted_prefs__",
            "androidx_security_crypto.master_key_keyset_prefs",
            "androidx_security_crypto.encrypted_prefs_keyset_prefs"
        )
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_MOCK_QKD = "mock_qkd_enabled"
        private const val KEY_MOCK_LOCATION = "mock_location_enabled"
    }
}

