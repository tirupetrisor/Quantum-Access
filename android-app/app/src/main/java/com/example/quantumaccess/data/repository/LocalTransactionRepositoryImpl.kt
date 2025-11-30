package com.example.quantumaccess.data.repository

import com.example.quantumaccess.data.local.dao.TransactionDao
import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import com.example.quantumaccess.data.local.mappers.toDomainModel
import com.example.quantumaccess.domain.model.AnalyticsCategory
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Repository local pentru tranzacții.
 * Implementează TransactionRepository și expune fluxuri reactive.
 */
class LocalTransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    // Flow expus conform cerințelor (chiar dacă nu e în interfața originală Domain)
    fun getTransactionsFlow(): Flow<List<TransactionHistoryEntry>> {
        return transactionDao.getAllFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertTransaction(amount: Double, mode: String, status: String, intercepted: Boolean) {
        val entity = LocalTransactionEntity(
            transactionId = UUID.randomUUID(),
            amount = amount,
            mode = mode,
            status = status,
            intercepted = intercepted,
            createdAt = Instant.now()
        )
        transactionDao.insert(entity)
    }

    // --- Implementare TransactionRepository (Sync - Legacy adaptation) ---

    override fun getTransactionHistory(): List<TransactionHistoryEntry> {
        // ATENȚIE: Blocăm firul de execuție pentru a respecta semnătura sincronă a interfeței existente.
        // Ideal, interfața ar trebui migrată la suspend sau Flow.
        return runBlocking {
            transactionDao.getAllFlow().first().map { it.toDomainModel() }
        }
    }

    override fun getSecurityDistribution(): List<TransactionAnalyticsSlice> {
        // Calculăm distribuția pe baza datelor din DB
        val transactions = getTransactionHistory()
        val total = transactions.size.toFloat()
        if (total == 0f) return emptyList()

        val quantum = transactions.count { it.channel == com.example.quantumaccess.domain.model.TransactionChannel.QUANTUM }.toFloat()
        val normal = transactions.count { it.channel == com.example.quantumaccess.domain.model.TransactionChannel.NORMAL }.toFloat()
        val intercepted = transactions.count { it.securityState == com.example.quantumaccess.domain.model.TransactionSecurityState.ALERT }.toFloat()

        return listOf(
            TransactionAnalyticsSlice("Quantum", (quantum / total) * 100, AnalyticsCategory.QUANTUM),
            TransactionAnalyticsSlice("Normal", (normal / total) * 100, AnalyticsCategory.NORMAL),
            TransactionAnalyticsSlice("Intercepted", (intercepted / total) * 100, AnalyticsCategory.INTERCEPTED)
        )
    }

    override fun getQuantumProcessSteps(): List<QuantumProcessStep> {
        // Returnăm pași statici sau din DB (momentan static pentru demo)
        return listOf(
            QuantumProcessStep(1.0f, "Completed", "Initialization", false),
            QuantumProcessStep(1.0f, "Completed", "Entanglement", false),
            QuantumProcessStep(1.0f, "Success", "QKD Exchange Secured", true)
        )
    }
}
