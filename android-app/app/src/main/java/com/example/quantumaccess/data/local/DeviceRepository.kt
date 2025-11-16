package com.example.quantumaccess.data.local

import android.content.Context
import android.provider.Settings

class DeviceRepository(
	private val context: Context,
	private val prefs: PreferencesManager
) {
	fun getOrCreateDeviceId(): String {
		prefs.deviceId?.let { return it }
		val androidId = Settings.Secure.getString(
			context.contentResolver,
			Settings.Secure.ANDROID_ID
		)
		prefs.deviceId = androidId
		return androidId
	}
}
