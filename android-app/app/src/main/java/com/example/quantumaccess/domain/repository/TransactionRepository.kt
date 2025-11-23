package com.example.quantumaccess.domain.repository

import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionHistoryEntry

interface TransactionRepository {
    fun getTransactionHistory(): List<TransactionHistoryEntry>
    fun getSecurityDistribution(): List<TransactionAnalyticsSlice>
    fun getQuantumProcessSteps(): List<QuantumProcessStep>
}

