package com.example.quantumaccess.data.repository

import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.local.dao.UserDao
import com.example.quantumaccess.data.local.entities.LocalUserEntity
import kotlinx.coroutines.delay
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
        email: String,
        password: String,
        deviceId: String,
        biometricEnabled: Boolean
    ): Result<Unit> {
        return try {
            if (useMock) {
                delay(600)
                
                // Validare unicitate username
                if (userDao.getByUsername(username) != null) {
                    return Result.failure(IllegalArgumentException("Username-ul este deja utilizat."))
                }

                // Validare unicitate email
                val existingUserByEmail = userDao.getByEmail(email)
                if (existingUserByEmail != null) {
                    // Dacă există un cont cu acest email, sugerăm autentificarea (sau login with Google)
                    return Result.failure(IllegalArgumentException("Există deja un cont cu acest email. Încearcă să te autentifici."))
                }
                
                val userId = UUID.randomUUID()
                val user = LocalUserEntity(
                    userId = userId,
                    username = username,
                    email = email,
                    name = name,
                    biometricEnabled = biometricEnabled,
                    googleId = null
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

    suspend fun googleSignIn(
        email: String,
        name: String,
        googleId: String,
        biometricEnabled: Boolean
    ): Result<Unit> {
        return try {
            if (useMock) {
                delay(800) // Simulate network
                
                // 1. Verificăm dacă există deja un user cu acest email
                val existingUser = userDao.getByEmail(email)
                
                if (existingUser != null) {
                    // Cont existent -> Unificăm conturile (asociem googleId dacă nu există)
                    // Actualizăm și biometric preference dacă utilizatorul o dorește explicit
                    val shouldUpdateBiometric = biometricEnabled && !existingUser.biometricEnabled
                    val shouldUpdateGoogleId = existingUser.googleId == null
                    
                    if (shouldUpdateGoogleId || shouldUpdateBiometric) {
                        val updatedUser = existingUser.copy(
                            googleId = googleId,
                            biometricEnabled = if (shouldUpdateBiometric) true else existingUser.biometricEnabled
                        )
                        userDao.update(updatedUser)
                    }
                    
                    if (biometricEnabled) {
                        prefs.setBiometricEnabled(true)
                    }
                } else {
                    // Cont nou -> Creăm user
                    // Generăm un username unic bazat pe email
                    var baseUsername = email.substringBefore("@")
                    var newUsername = baseUsername
                    var counter = 1
                    while (userDao.getByUsername(newUsername) != null) {
                        newUsername = "$baseUsername$counter"
                        counter++
                    }

                    val userId = UUID.randomUUID()
                    val newUser = LocalUserEntity(
                        userId = userId,
                        username = newUsername, // Username generat automat
                        email = email,
                        name = name,
                        biometricEnabled = biometricEnabled,
                        googleId = googleId
                    )
                    userDao.insert(newUser)
                    
                    if (biometricEnabled) {
                        prefs.setBiometricEnabled(true)
                    }
                }
                
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Backend not configured"))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
