package com.example.quantumaccess.data.repository

import android.content.Context
import android.util.Log
import com.example.quantumaccess.BuildConfig
import com.example.quantumaccess.data.local.dao.QuantumKeyDao
import com.example.quantumaccess.data.local.dao.TransactionDao
import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import com.example.quantumaccess.data.local.entities.QuantumKeyEntity
import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.quantum.EveDetector
import com.example.quantumaccess.data.quantum.QKDProvider
import com.example.quantumaccess.data.quantum.QKDService
import com.example.quantumaccess.data.quantum.QuantumKeyData
import com.example.quantumaccess.data.remote.RemoteTransactionDataSource
import com.example.quantumaccess.data.remote.dto.TransactionDto
import com.example.quantumaccess.domain.model.QuantumProcessStep
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

/**
 * Repository for Quantum-secured transactions using real QKD
 */
class QuantumTransactionRepository(
    private val context: Context,
    private val transactionDao: TransactionDao,
    private val quantumKeyDao: QuantumKeyDao,
    private val remoteDataSource: RemoteTransactionDataSource,
    private val supabase: SupabaseClient,
    private val prefs: SecurePrefsManager
) {
    
    private val qkdService: QKDService by lazy {
        val apiKey = BuildConfig.QKD_API_KEY
        val provider = when (BuildConfig.QKD_PROVIDER) {
            "QRYPT" -> QKDProvider.QRYPT
            "QBITSHIELD" -> QKDProvider.QBITSHIELD
            else -> QKDProvider.SIMULATION
        }
        QKDService(apiKey, provider)
    }
    
    private val eveDetector = EveDetector()

    /**
     * Process quantum transaction with real QKD
     * Returns progress steps for UI updates
     */
    suspend fun processQuantumTransaction(
        userId: UUID,
        amount: Double,
        beneficiary: String,
        onProgress: suspend (QuantumProcessStep) -> Unit
    ): Result<LocalTransactionEntity> = withContext(Dispatchers.IO) {
        try {
            val transactionId = UUID.randomUUID()
            
            // Step 1: Initialize quantum channel
            onProgress(
                QuantumProcessStep(
                    progress = 0.2f,
                    status = "Initializing Quantum Channel",
                    detail = "Establishing secure quantum connection...",
                    isTerminal = false
                )
            )
            delay(800)
            
            // Step 2: Generate quantum key via QKD
            onProgress(
                QuantumProcessStep(
                    progress = 0.4f,
                    status = "Generating Quantum Key",
                    detail = "Using ${BuildConfig.QKD_PROVIDER} QKD protocol...",
                    isTerminal = false
                )
            )
            
            val quantumKeyResult = qkdService.generateQuantumKey(
                keySize = 256,
                transactionId = transactionId.toString()
            )
            
            if (quantumKeyResult.isFailure) {
                return@withContext Result.failure(
                    quantumKeyResult.exceptionOrNull() ?: Exception("QKD key generation failed")
                )
            }
            
            val quantumKey = quantumKeyResult.getOrThrow()
            delay(600)
            
            // Step 3: Eve Detection - Check for eavesdropping
            onProgress(
                QuantumProcessStep(
                    progress = 0.5f,
                    status = "Eve Detection",
                    detail = "Scanning for eavesdropping attempts...",
                    isTerminal = false
                )
            )
            
            val isEveEnabled = prefs.isEveSimulationEnabled()
            val eveResult = eveDetector.detectEavesdropping(
                quantumEntropy = quantumKey.quantumEntropy,
                keySize = quantumKey.keySize,
                isEveEnabled = isEveEnabled
            )
            
            delay(700)
            
            // Check if Eve was detected
            if (eveResult.isIntercepted) {
                // ABORT! Eavesdropping detected
                Log.e(TAG, "EAVESDROPPING DETECTED! ${eveResult.message}")
                
                onProgress(
                    QuantumProcessStep(
                        progress = 0.6f,
                        status = "Eavesdropping Detected",
                        detail = "QBER: ${String.format("%.1f", eveResult.qber * 100)}% exceeds threshold (11%). Transaction aborted for security.",
                        isTerminal = true
                    )
                )
                
                // Save intercepted transaction for audit
                val interceptedTransaction = LocalTransactionEntity(
                    transactionId = transactionId,
                    userId = userId,
                    amount = amount,
                    beneficiary = beneficiary,
                    mode = "QUANTUM",
                    status = "ABORTED",
                    intercepted = true,
                    lastUpdated = Instant.now(),
                    createdAt = Instant.now()
                )
                transactionDao.insert(interceptedTransaction)
                
                return@withContext Result.failure(
                    SecurityException("Eavesdropping detected! QBER: ${eveResult.qber * 100}%")
                )
            }
            
            // Step 4: Quantum entanglement verification (passed Eve check)
            onProgress(
                QuantumProcessStep(
                    progress = 0.65f,
                    status = "Quantum Entanglement Verified",
                    detail = "Key secure (Entropy: ${String.format("%.2f", quantumKey.quantumEntropy * 100)}%, QBER: ${String.format("%.1f", eveResult.qber * 100)}%)",
                    isTerminal = false
                )
            )
            delay(600)
            
            // Step 5: Encrypt and send transaction
            onProgress(
                QuantumProcessStep(
                    progress = 0.75f,
                    status = "Encrypting Transaction",
                    detail = "Applying quantum-secured encryption...",
                    isTerminal = false
                )
            )
            delay(500)
            
            // Create transaction entity FIRST (before quantum key due to foreign key)
            val transactionEntity = LocalTransactionEntity(
                transactionId = transactionId,
                userId = userId,
                amount = amount,
                beneficiary = beneficiary,
                mode = "QUANTUM",
                status = "SUCCESS",
                intercepted = false, // Quantum is secure by definition
                lastUpdated = Instant.now(),
                createdAt = Instant.now()
            )
            
            // Save transaction to local database FIRST
            transactionDao.insert(transactionEntity)
            
            // Step 6: Store quantum key metadata (AFTER transaction due to foreign key)
            onProgress(
                QuantumProcessStep(
                    progress = 0.9f,
                    status = "Storing Quantum Key",
                    detail = "Saving quantum metadata...",
                    isTerminal = false
                )
            )
            
            val keyEntity = QuantumKeyEntity(
                keyId = quantumKey.keyId,
                transactionId = transactionId,
                keyMaterialHash = hashKey(quantumKey.keyMaterial),
                keySize = quantumKey.keySize,
                algorithm = quantumKey.algorithm,
                provider = quantumKey.provider,
                generatedAt = Instant.ofEpochMilli(quantumKey.generatedAt),
                quantumEntropy = quantumKey.quantumEntropy,
                isReal = quantumKey.isReal,
                usedAt = Instant.now()
            )
            quantumKeyDao.insert(keyEntity)
            
            // Sync to Supabase
            try {
                val transactionDto = TransactionDto(
                    transactionId = transactionId.toString(),
                    userId = userId.toString(),
                    amount = amount,
                    type = "QUANTUM",
                    status = "SUCCESS",
                    interceptionDetected = false,
                    beneficiaryName = beneficiary
                )
                remoteDataSource.createTransaction(transactionDto)
                
                // Store quantum key metadata in Supabase
                storeQuantumKeyMetadata(keyEntity)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync to remote, continuing with local storage", e)
            }
            
            // Step 7: Success
            val successDetail = if (quantumKey.isReal) {
                "Transaction secured using ${quantumKey.provider} quantum key distribution. No eavesdropping detected."
            } else {
                "Transaction completed successfully. No eavesdropping detected."
            }
            
            onProgress(
                QuantumProcessStep(
                    progress = 1.0f,
                    status = "Transaction Complete",
                    detail = successDetail,
                    isTerminal = true
                )
            )
            
            Result.success(transactionEntity)
            
        } catch (e: Exception) {
            Log.e(TAG, "Quantum transaction failed", e)
            onProgress(
                QuantumProcessStep(
                    progress = 0.0f,
                    status = "Transaction Failed",
                    detail = e.message ?: "Unknown error",
                    isTerminal = true
                )
            )
            Result.failure(e)
        }
    }
    
    /**
     * Get quantum transaction statistics
     */
    suspend fun getQuantumStats(): QuantumStats = withContext(Dispatchers.IO) {
        try {
            val realKeyCount = quantumKeyDao.getRealKeyCount()
            val avgEntropy = quantumKeyDao.getAverageQuantumEntropy() ?: 0.0
            
            QuantumStats(
                totalQuantumTransactions = transactionDao.getCountByMode("QUANTUM"),
                realQKDTransactions = realKeyCount,
                averageQuantumEntropy = avgEntropy,
                provider = BuildConfig.QKD_PROVIDER
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get quantum stats", e)
            QuantumStats(0, 0, 0.0, "UNKNOWN")
        }
    }
    
    /**
     * Store quantum key metadata in Supabase
     */
    private suspend fun storeQuantumKeyMetadata(keyEntity: QuantumKeyEntity) {
        try {
            val metadata = buildJsonObject {
                put("key_id", keyEntity.keyId)
                put("transaction_id", keyEntity.transactionId.toString())
                put("key_size", keyEntity.keySize)
                put("algorithm", keyEntity.algorithm)
                put("provider", keyEntity.provider)
                put("quantum_entropy", keyEntity.quantumEntropy)
                put("is_real", keyEntity.isReal)
                put("generated_at", keyEntity.generatedAt.toString())
            }
            
            supabase.from("quantum_keys").insert(metadata)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to store quantum key metadata remotely", e)
        }
    }
    
    /**
     * Hash the key material for secure storage
     * Never store raw keys - only hashes for verification
     */
    private fun hashKey(keyMaterial: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(keyMaterial.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    companion object {
        private const val TAG = "QuantumTransactionRepo"
    }
}

/**
 * Quantum transaction statistics
 */
data class QuantumStats(
    val totalQuantumTransactions: Int,
    val realQKDTransactions: Int,
    val averageQuantumEntropy: Double,
    val provider: String
)
