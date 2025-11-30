package com.example.quantumaccess.domain.repository

import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionHistory(): List<TransactionHistoryEntry>
    fun observeTransactionHistory(): Flow<List<TransactionHistoryEntry>>
    fun getSecurityDistribution(): List<TransactionAnalyticsSlice>
    fun getQuantumProcessSteps(): List<QuantumProcessStep>
    
    // Add support for inserting transactions
    suspend fun insertTransaction(
        amount: Double,
        mode: String,
        status: String,
        intercepted: Boolean,
        beneficiary: String
    )
}
