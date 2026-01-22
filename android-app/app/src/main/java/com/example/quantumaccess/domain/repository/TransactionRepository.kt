package com.example.quantumaccess.domain.repository

import com.example.quantumaccess.domain.model.ComparisonTimelineStep
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.SecurityScoreSummary
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.model.TransactionRequest
import com.example.quantumaccess.domain.model.TransactionResult
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

    // ===== New methods for security features =====

    /**
     * Procesează o tranzacție completă cu calcul de scoruri de securitate.
     * Apelează QKDService și EveDetector (sau stub-uri) pentru a obține rezultatele.
     */
    suspend fun processTransaction(request: TransactionRequest): Result<TransactionResult>

    /**
     * Obține sumarul scorurilor de securitate (pentru Dashboard).
     * Calculează media sau ultima valoare pentru scorurile Normal și Quantum.
     */
    fun getSecurityScoreSummary(): SecurityScoreSummary

    /**
     * Obține pașii pentru timeline-ul de comparație Normal vs Quantum.
     * Folosit în ecranul Analytics pentru a vizualiza diferențele.
     */
    fun getComparisonTimelineSteps(): List<ComparisonTimelineStep>

    /**
     * Obține ultima tranzacție pentru un utilizator (pentru overlay).
     */
    fun getLastTransaction(): TransactionHistoryEntry?

    /**
     * Setează/Obține starea toggle-ului "Simulează atac cuantic".
     */
    var simulateAttackEnabled: Boolean
}
