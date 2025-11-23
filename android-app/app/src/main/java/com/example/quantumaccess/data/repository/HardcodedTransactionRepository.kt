package com.example.quantumaccess.data.repository

import com.example.quantumaccess.data.source.hardcoded.HardcodedTransactionDataSource
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.repository.TransactionRepository

class HardcodedTransactionRepository(
    private val dataSource: HardcodedTransactionDataSource = HardcodedTransactionDataSource
) : TransactionRepository {

    override fun getTransactionHistory(): List<TransactionHistoryEntry> = dataSource.transactionHistory

    override fun getSecurityDistribution(): List<TransactionAnalyticsSlice> = dataSource.analyticsSlices

    override fun getQuantumProcessSteps(): List<QuantumProcessStep> = dataSource.quantumSteps
}

