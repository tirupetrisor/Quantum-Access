package com.example.quantumaccess.domain.repository

import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    suspend fun getDeviceId(): String
    suspend fun isBiometricEnabled(): Boolean
    suspend fun setBiometricEnabled(enabled: Boolean)
    
    // Demo flags
    suspend fun isMockQkdEnabled(): Boolean
    suspend fun isMockLocationEnabled(): Boolean
}

