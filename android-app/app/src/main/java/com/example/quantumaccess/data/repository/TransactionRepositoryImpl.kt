package com.example.quantumaccess.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.quantumaccess.data.local.dao.TransactionDao
import com.example.quantumaccess.data.local.dao.UserDao
import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import com.example.quantumaccess.data.local.mappers.toDomainModel
import com.example.quantumaccess.data.remote.RemoteTransactionDataSource
import com.example.quantumaccess.data.remote.dto.TransactionDto
import com.example.quantumaccess.domain.model.AnalyticsCategory
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.repository.TransactionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID

class TransactionRepositoryImpl(
    private val context: Context,
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val remoteDataSource: RemoteTransactionDataSource,
    private val supabase: SupabaseClient
) : TransactionRepository {

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override suspend fun syncTransactions(userId: UUID) {
        if (!isOnline()) return

        try {
            val remoteTransactions = remoteDataSource.getTransactions(userId.toString())
            if (remoteTransactions.isEmpty()) {
                return
            }

            val localEntities = mutableListOf<LocalTransactionEntity>()
            for (dto in remoteTransactions) {
                val resolvedId = dto.transactionId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
                val existing = transactionDao.getById(resolvedId)
                val beneficiaryValue = dto.beneficiaryName ?: existing?.beneficiary ?: "External"
                localEntities += dto.toLocalEntity(
                    userId = userId,
                    beneficiary = beneficiaryValue,
                    overrideId = resolvedId
                )
            }
            transactionDao.insertAll(localEntities)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync transactions", e)
        }
    }

    override suspend fun createTransaction(
        userId: UUID,
        amount: Double,
        mode: String,
        status: String,
        intercepted: Boolean,
        beneficiary: String
    ): Result<Unit> {
        if (!isOnline()) {
            val offlineError = IllegalStateException("Offline mode: Cannot create transactions.")
            Log.w(TAG, offlineError.message ?: "Offline mode")
            return Result.failure(offlineError)
        }

        return try {
            val generatedTransactionId = UUID.randomUUID().toString()
            val dto = TransactionDto(
                transactionId = generatedTransactionId,
                userId = userId.toString(),
                amount = amount,
                type = mode,
                status = status,
                interceptionDetected = intercepted,
                beneficiaryName = beneficiary
            )

            val createdTransaction = remoteDataSource.createTransaction(dto)
            val normalizedTransaction = if (createdTransaction.transactionId.isNullOrBlank()) {
                createdTransaction.copy(transactionId = generatedTransactionId)
            } else {
                createdTransaction
            }
            cacheTransaction(
                normalizedTransaction.copy(
                    beneficiaryName = normalizedTransaction.beneficiaryName ?: beneficiary
                ),
                userId,
                beneficiary
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create transaction", e)
            Result.failure(e)
        }
    }

    // Legacy support for existing UI
    override suspend fun insertTransaction(
        amount: Double,
        mode: String,
        status: String,
        intercepted: Boolean,
        beneficiary: String
    ): Result<Unit> {
        val user = supabase.auth.currentSessionOrNull()?.user
            ?: return Result.failure(IllegalStateException("Cannot create transaction: User not logged in."))

        return createTransaction(
            userId = UUID.fromString(user.id),
            amount = amount,
            mode = mode,
            status = status,
            intercepted = intercepted,
            beneficiary = beneficiary
        )
    }

    override fun observeTransactionHistory(userId: UUID?): Flow<List<TransactionHistoryEntry>> {
        // 1. Use explicit userId if provided
        if (userId != null) {
            return transactionDao.getTransactions(userId).map { entities ->
                entities.map { it.toDomainModel() }
            }
        }

        // 2. Use Supabase session if available
        val remoteId = supabase.auth.currentSessionOrNull()?.user?.id?.let { UUID.fromString(it) }
        if (remoteId != null) {
            return transactionDao.getTransactions(remoteId).map { entities ->
                entities.map { it.toDomainModel() }
            }
        }

        // 3. Fallback to local active user (Offline Mode)
        return userDao.getCurrentUser().flatMapLatest { user ->
            if (user != null) {
                transactionDao.getTransactions(user.userId)
            } else {
                flowOf(emptyList())
            }
        }.map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTransactionHistory(): List<TransactionHistoryEntry> {
        val remoteId = supabase.auth.currentSessionOrNull()?.user?.id?.let { UUID.fromString(it) }
        val targetUserId = remoteId ?: runBlocking {
            userDao.getCurrentUser().first()?.userId
        }

        if (targetUserId == null) return emptyList()
        
        return runBlocking {
            transactionDao.getTransactions(targetUserId).first().map { it.toDomainModel() }
        }
    }

    override fun getSecurityDistribution(): List<TransactionAnalyticsSlice> {
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
        return listOf(
            QuantumProcessStep(1.0f, "Completed", "Initialization", false),
            QuantumProcessStep(1.0f, "Completed", "Entanglement", false),
            QuantumProcessStep(1.0f, "Success", "QKD Exchange Secured", true)
        )
    }

    private suspend fun cacheTransaction(dto: TransactionDto, userId: UUID, beneficiary: String) {
        val entity = dto.toLocalEntity(
            userId = userId,
            beneficiary = dto.beneficiaryName ?: beneficiary
        )
        transactionDao.insert(entity)
    }

    private fun TransactionDto.toLocalEntity(
        userId: UUID,
        beneficiary: String,
        overrideId: UUID? = null
    ): LocalTransactionEntity {
        val resolvedId = overrideId ?: transactionId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        val resolvedCreatedAt = createdAt?.let { Instant.parse(it) } ?: Instant.now()
        val safeInterceptionStatus = if (type.equals("NORMAL", ignoreCase = true)) {
            false
        } else {
            interceptionDetected
        }
        return LocalTransactionEntity(
            transactionId = resolvedId,
            userId = userId,
            amount = amount,
            beneficiary = beneficiary,
            mode = type,
            status = status,
            intercepted = safeInterceptionStatus,
            lastUpdated = Instant.now(),
            createdAt = resolvedCreatedAt
        )
    }

    companion object {
        private const val TAG = "TransactionRepository"
    }
}
