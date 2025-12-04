package com.example.quantumaccess.domain.repository

import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface TransactionRepository {
    fun getTransactionHistory(): List<TransactionHistoryEntry>
    fun observeTransactionHistory(userId: UUID? = null): Flow<List<TransactionHistoryEntry>>
    fun getSecurityDistribution(): List<TransactionAnalyticsSlice>
    fun getQuantumProcessSteps(): List<QuantumProcessStep>
    
    // Sync
    suspend fun syncTransactions(userId: UUID)

    // Create (Remote) - Strict
    suspend fun createTransaction(
        userId: UUID,
        amount: Double,
        mode: String,
        status: String,
        intercepted: Boolean,
        beneficiary: String
    ): Result<Unit>

    // Legacy/Convenience (Simulates/Adopts current user)
    suspend fun insertTransaction(
        amount: Double,
        mode: String,
        status: String,
        intercepted: Boolean,
        beneficiary: String
    ): Result<Unit>
}
