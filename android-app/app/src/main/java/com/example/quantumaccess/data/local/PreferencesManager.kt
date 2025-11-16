package com.example.quantumaccess.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(context: Context) {
	private val masterKey = MasterKey.Builder(context)
		.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
		.build()

	private val prefs = EncryptedSharedPreferences.create(
		context,
		"quantum_prefs",
		masterKey,
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)

	var userId: String?
		get() = prefs.getString("user_id", null)
		set(value) { prefs.edit().putString("user_id", value).apply() }

	var biometricEnabled: Boolean
		get() = prefs.getBoolean("biometric_enabled", true)
		set(value) { prefs.edit().putBoolean("biometric_enabled", value).apply() }

	var deviceId: String?
		get() = prefs.getString("device_id", null)
		set(value) { prefs.edit().putString("device_id", value).apply() }

	var registeredUsernames: Set<String>
		get() = prefs.getStringSet("registered_usernames", emptySet()) ?: emptySet()
		set(value) { prefs.edit().putStringSet("registered_usernames", value).apply() }
}
