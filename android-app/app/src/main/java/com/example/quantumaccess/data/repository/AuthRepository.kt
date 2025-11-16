package com.example.quantumaccess.data.repository

import com.example.quantumaccess.data.local.PreferencesManager
import kotlinx.coroutines.delay
import java.util.UUID

class AuthRepository(
	private val prefs: PreferencesManager,
	private val useMock: Boolean = true
) {
	suspend fun register(
		name: String,
		username: String,
		password: String,
		deviceId: String,
		biometricEnabled: Boolean
	): Result<Unit> {
		return try {
			if (useMock) {
				delay(600)
				val normalized = username.trim().lowercase()
				val existing = prefs.registeredUsernames
				if (normalized in existing) {
					return Result.failure(IllegalStateException("Username already exists (local)"))
				}
				val updated = existing.toMutableSet().apply { add(normalized) }.toSet()
				prefs.registeredUsernames = updated

				val generatedUserId = UUID.randomUUID().toString()
				prefs.userId = generatedUserId
				prefs.biometricEnabled = biometricEnabled
				Result.success(Unit)
			} else {
				// Placeholder pentru integrare backend ulterioară
				Result.failure(IllegalStateException("Backend not configured"))
			}
		} catch (t: Throwable) {
			Result.failure(t)
		}
	}
}


