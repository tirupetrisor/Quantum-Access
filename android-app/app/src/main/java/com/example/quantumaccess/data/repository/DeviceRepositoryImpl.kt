package com.example.quantumaccess.data.repository

import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.local.dao.UserDao
import com.example.quantumaccess.domain.repository.DeviceRepository
import javax.inject.Inject

/**
 * Implementarea repository-ului pentru dispozitiv.
 * Interacționează cu SecurePrefsManager pentru setări locale.
 */
class DeviceRepositoryImpl @Inject constructor(
    private val prefsManager: SecurePrefsManager,
    private val userDao: UserDao
) : DeviceRepository {

    override suspend fun getDeviceId(): String {
        return prefsManager.getDeviceId()
    }

    override suspend fun isBiometricEnabled(): Boolean {
        return prefsManager.isBiometricEnabled()
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        prefsManager.setBiometricEnabled(enabled)
        // Opțional: actualizăm și entitatea LocalUserEntity dacă există logică sincronizată
    }

    override suspend fun isMockQkdEnabled(): Boolean {
        return prefsManager.isMockQkdEnabled()
    }

    override suspend fun isMockLocationEnabled(): Boolean {
        return prefsManager.isMockLocationEnabled()
    }
}

