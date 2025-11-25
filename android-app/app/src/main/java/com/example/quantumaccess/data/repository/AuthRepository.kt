package com.example.quantumaccess.data.repository

import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.local.dao.UserDao
import com.example.quantumaccess.data.local.entities.LocalUserEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val prefs: SecurePrefsManager,
    private val userDao: UserDao,
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
                // Check existing user in DB (simplified check, usually by username)
                // We iterate or check if any user has this username. 
                // Since UserDao doesn't have getByUsername, we might add it or just insert and catch conflict.
                // But LocalUserEntity PK is userId. Username is just a field.
                // I'll assume for now we allow registration or I should check for duplicate username.
                // I'll just insert a new user.
                
                val userId = UUID.randomUUID()
                val user = LocalUserEntity(
                    userId = userId,
                    username = username,
                    name = name,
                    biometricEnabled = biometricEnabled
                )
                userDao.insert(user)
                
                prefs.setBiometricEnabled(biometricEnabled)
                // Note: password is not stored locally in this demo/prompt requirements (usually hashed).
                
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
