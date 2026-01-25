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
import com.example.quantumaccess.data.remote.dto.SecurityAnalysisDto
import com.example.quantumaccess.data.remote.dto.TransactionDto
import com.example.quantumaccess.data.remote.dto.TransactionProcessingDto
import com.example.quantumaccess.domain.model.AnalyticsCategory
import com.example.quantumaccess.domain.model.ComparisonTimelineStep
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.SecurityScoreSummary
import com.example.quantumaccess.domain.model.TimelineStepStatus
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionScenario
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.model.TransactionRequest
import com.example.quantumaccess.domain.model.TransactionResult
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random
import com.example.quantumaccess.core.security.SecurityScorer

class TransactionRepositoryImpl(
    private val context: Context,
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val remoteDataSource: RemoteTransactionDataSource,
    private val supabase: SupabaseClient
) : TransactionRepository {

    // Toggle pentru simularea atacului cuantic (demo)
    override var simulateAttackEnabled: Boolean = false

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override suspend fun syncTransactions(userId: UUID) {
        if (!isOnline()) return

        try {
            // Obținem tranzacțiile din Supabase
            val remoteTransactions = remoteDataSource.getTransactions(userId.toString())
            if (remoteTransactions.isEmpty()) {
                Log.d(TAG, "No remote transactions to sync")
                return
            }

            val localEntities = mutableListOf<LocalTransactionEntity>()
            for (dto in remoteTransactions) {
                val resolvedId = dto.transactionId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
                val existing = transactionDao.getById(resolvedId)
                val beneficiaryValue = dto.beneficiaryName
                    ?: (listOfNotNull(
                        dto.patientId?.let { "Patient: $it" },
                        dto.accessReason?.let { "Reason: $it" }
                    ).joinToString(" | ").ifEmpty { null } ?: existing?.beneficiary ?: "External")
                val resolvedCreatedAt = dto.createdAt?.let {
                    try { Instant.parse(it) } catch (e: Exception) { Instant.now() }
                } ?: Instant.now()
                val amountVal = dto.amount ?: 0.0

                val processing = try {
                    remoteDataSource.getProcessing(resolvedId.toString())
                } catch (e: Exception) {
                    Log.w(TAG, "Could not get processing for $resolvedId", e)
                    null
                }
                val security = try {
                    remoteDataSource.getSecurityAnalysis(resolvedId.toString())
                } catch (e: Exception) {
                    Log.w(TAG, "Could not get security for $resolvedId", e)
                    null
                }

                localEntities += LocalTransactionEntity(
                    transactionId = resolvedId,
                    userId = userId,
                    amount = amountVal,
                    beneficiary = beneficiaryValue,
                    mode = processing?.processingMode ?: "NORMAL",
                    status = processing?.status ?: "SUCCESS",
                    intercepted = security?.eveDetected ?: false,
                    lastUpdated = Instant.now(),
                    createdAt = resolvedCreatedAt,
                    scenario = dto.scenario,
                    securityScoreNormal = security?.securityScoreNormal,
                    securityScoreQuantum = security?.securityScoreQuantum,
                    qber = security?.qber,
                    eveDetected = security?.eveDetected,
                    compromised = security?.compromised,
                    patientId = dto.patientId,
                    accessReason = dto.accessReason
                )
            }
            transactionDao.insertAll(localEntities)
            Log.d(TAG, "Synced ${localEntities.size} transactions")
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
            
            // DTO pentru transactions
            val transactionDto = TransactionDto(
                transactionId = generatedTransactionId,
                userId = userId.toString(),
                amount = amount,
                beneficiaryName = beneficiary
            )

            // DTO pentru transaction_processing
            val processingDto = TransactionProcessingDto(
                transactionId = generatedTransactionId,
                processingMode = mode,
                status = status
            )

            // DTO pentru security_analysis (valori default)
            val securityDto = SecurityAnalysisDto(
                transactionId = generatedTransactionId,
                eveDetected = intercepted,
                compromised = intercepted
            )

            val createdTransaction = remoteDataSource.createFullTransaction(
                transaction = transactionDto,
                processing = processingDto,
                securityAnalysis = securityDto
            )
            
            cacheTransactionWithSecurityFields(
                createdTransaction,
                processingDto,
                securityDto,
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
        // Determine if we should simulate Eve based on the flag
        val simulateEve = simulateAttackEnabled
        val qberValue = if (simulateEve) Random.nextDouble(0.15, 0.25) else Random.nextDouble(0.02, 0.06)
        val eveDetected = qberValue > 0.11
        
        return listOf(
            // Step 1: Initialize quantum channel
            QuantumProcessStep(
                progress = 0.10f,
                status = "Initializing Quantum Channel",
                detail = "Establishing secure connection to QKD server...",
                isTerminal = false
            ),
            // Step 2: Generate quantum key
            QuantumProcessStep(
                progress = 0.25f,
                status = "Generating Quantum Key",
                detail = "BB84 protocol active - transmitting qubits...",
                isTerminal = false
            ),
            // Step 3: Qubit transmission
            QuantumProcessStep(
                progress = 0.40f,
                status = "Transmitting Qubits",
                detail = "Alice → Bob: Polarized photons in transit...",
                isTerminal = false
            ),
            // Step 4: Basis reconciliation
            QuantumProcessStep(
                progress = 0.55f,
                status = "Basis Reconciliation",
                detail = "Comparing measurement bases between parties...",
                isTerminal = false
            ),
            // Step 5: Eve Detection / QBER calculation
            QuantumProcessStep(
                progress = 0.70f,
                status = if (eveDetected) "⚠️ Eavesdropping Detected!" else "Scanning for Eve",
                detail = "QBER: ${String.format("%.1f", qberValue * 100)}% ${if (eveDetected) "(threshold exceeded!)" else "(within safe limits)"}",
                isTerminal = false
            ),
            // Step 6: Privacy amplification
            QuantumProcessStep(
                progress = 0.85f,
                status = if (eveDetected) "Regenerating Secure Key" else "Privacy Amplification",
                detail = if (eveDetected) "Attack blocked - generating new quantum key..." else "Applying error correction and key distillation...",
                isTerminal = false
            ),
            // Step 7: Final result
            QuantumProcessStep(
                progress = 1.0f,
                status = if (eveDetected) "Transaction Secured (Attack Blocked)" else "Transaction Complete",
                detail = if (eveDetected) 
                    "QKD detected interception attempt. New key generated. QBER: ${String.format("%.1f", qberValue * 100)}%" 
                else 
                    "Quantum-secured transaction successful. QBER: ${String.format("%.1f", qberValue * 100)}%",
                isTerminal = true
            )
        )
    }


    // ===== New methods for security features =====

    override suspend fun processTransaction(request: TransactionRequest): Result<TransactionResult> {
        val user = supabase.auth.currentSessionOrNull()?.user
            ?: return Result.failure(IllegalStateException("User not logged in"))

        val userId = UUID.fromString(user.id)
        val transactionId = UUID.randomUUID().toString()
        val mode = if (request.mode == TransactionChannel.QUANTUM) "QUANTUM" else "NORMAL"

        // Simulăm scorurile de securitate (în producție, acestea ar veni de la QKDService/EveDetector)
        val securityResult = simulateSecurityAnalysis(request)

        // Determinăm statusul tranzacției
        val status = if (securityResult.compromised) "INTERCEPTED" else "SUCCESS"

        // DTO pentru tabela transactions: Banking = amount + beneficiaryName; Medical = patientId + accessReason, amount = null
        val isMedical = request.scenario == TransactionScenario.MEDICAL_RECORD_ACCESS
        // Pentru tranzacții bancare, asigurăm că avem un amount valid
        val bankingAmount = if (isMedical) null else (request.amount ?: 0.0)
        val transactionDto = TransactionDto(
            transactionId = transactionId,
            userId = userId.toString(),
            amount = bankingAmount,
            beneficiaryName = if (isMedical) null else (request.beneficiary ?: "Unknown"),
            scenario = request.scenario.name,
            patientId = if (isMedical) request.patientId else null,
            accessReason = if (isMedical) request.accessReason else null
        )

        // DTO pentru tabela transaction_processing
        val processingDto = TransactionProcessingDto(
            transactionId = transactionId,
            processingMode = mode,
            status = status
        )

        // DTO pentru tabela security_analysis
        val securityDto = SecurityAnalysisDto(
            transactionId = transactionId,
            securityScoreNormal = securityResult.normalScore,
            securityScoreQuantum = securityResult.quantumScore,
            qber = securityResult.qber,
            eveDetected = securityResult.eveDetected,
            compromised = securityResult.compromised
        )

        // Always save locally (regardless of online/offline)
        val beneficiaryDisplay = if (isMedical) {
            listOfNotNull(request.patientId?.let { "Patient: $it" }, request.accessReason?.let { "Reason: $it" }).joinToString(" | ")
                .ifEmpty { "Medical Access" }
        } else {
            request.beneficiary ?: "Unknown"
        }
        val amountForEntity = request.amount ?: 0.0
        val entity = LocalTransactionEntity(
            transactionId = UUID.fromString(transactionId),
            userId = userId,
            amount = amountForEntity,
            beneficiary = beneficiaryDisplay,
            mode = mode,
            status = status,
            intercepted = securityResult.eveDetected,
            createdAt = Instant.now(),
            scenario = request.scenario.name,
            securityScoreNormal = securityResult.normalScore,
            securityScoreQuantum = securityResult.quantumScore,
            qber = securityResult.qber,
            eveDetected = securityResult.eveDetected,
            compromised = securityResult.compromised,
            patientId = request.patientId,
            accessReason = request.accessReason
        )
        
        return try {
            // Salvăm local PRIMA DATĂ
            transactionDao.insert(entity)
            Log.d(TAG, "Transaction saved locally: $transactionId")
            
            // Apoi încercăm să salvăm și în Supabase
            if (isOnline()) {
                try {
                    Log.d(TAG, "Attempting to save transaction to Supabase: $transactionId")
                    Log.d(TAG, "Transaction DTO: userId=${transactionDto.userId}, amount=${transactionDto.amount}, beneficiary=${transactionDto.beneficiaryName}, scenario=${transactionDto.scenario}")
                    Log.d(TAG, "Processing DTO: mode=${processingDto.processingMode}, status=${processingDto.status}")
                    Log.d(TAG, "Security DTO: normalScore=${securityDto.securityScoreNormal}, quantumScore=${securityDto.securityScoreQuantum}, eveDetected=${securityDto.eveDetected}")
                    
                    remoteDataSource.createFullTransaction(
                        transaction = transactionDto,
                        processing = processingDto,
                        securityAnalysis = securityDto
                    )
                    Log.d(TAG, "Transaction saved successfully to Supabase: $transactionId")
                } catch (remoteEx: Exception) {
                    // Dacă Supabase fail, tranzacția e salvată local dar trebuie să logăm eroarea detaliat
                    Log.e(TAG, "CRITICAL: Failed to save to Supabase (local saved): $transactionId", remoteEx)
                    Log.e(TAG, "Error type: ${remoteEx.javaClass.simpleName}")
                    Log.e(TAG, "Error message: ${remoteEx.message}")
                    remoteEx.printStackTrace()
                    // Nu aruncăm eroarea aici pentru că vrem ca tranzacția să fie disponibilă local
                    // dar logăm detaliat pentru debugging
                }
            } else {
                Log.w(TAG, "Device is offline, transaction saved only locally: $transactionId")
            }

            // Log event
            Log.i(TAG, "security_score_computed: normal=${securityResult.normalScore}, quantum=${securityResult.quantumScore}")
            if (simulateAttackEnabled) {
                Log.i(TAG, "quantum_attack_simulated: enabled=true")
            }
            if (securityResult.eveDetected) {
                Log.w(TAG, "eavesdrop_detected: qber=${securityResult.qber}")
            }

            Result.success(securityResult)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process transaction", e)
            Result.failure(e)
        }
    }

    /**
     * Analizează securitatea tranzacției folosind SecurityScorer.
     * Folosește logica reală pentru calcul scoruri conform specificației.
     */
    private fun simulateSecurityAnalysis(request: TransactionRequest): TransactionResult {
        val isQuantum = request.mode == TransactionChannel.QUANTUM
        val keySize = 256 // Standard key size
        val scenario = request.scenario
        val shouldSimulateAttack = simulateAttackEnabled || request.simulateAttack
        
        var qber: Double
        var eveDetected = false
        var compromised = false

        if (isQuantum) {
            // === QUANTUM MODE ===
            // Simulează generare cheie QKD
            qber = if (shouldSimulateAttack) {
                // Atac simulat: QBER ridicat (0.15-0.30)
                Random.nextDouble(0.15, 0.30)
            } else {
                // Normal: QBER scăzut (0.01-0.05)
                Random.nextDouble(0.01, 0.05)
            }
            
            // Eve Detection: verifică dacă QBER depășește threshold-ul
            // Conform specificației: qber > 0.10 sau (isEveEnabled && qber > 0.05)
            eveDetected = qber > 0.10 || (shouldSimulateAttack && qber > 0.05)
            
            // Quantum NU este compromis chiar dacă Eve e detectat (cheia se regenerează)
            compromised = false
        } else {
            // === NORMAL MODE ===
            // NU folosește QKD, deci nu poate detecta Eve
            eveDetected = false
            
            if (shouldSimulateAttack) {
                // Atac simulat: QBER ridicat pentru comparație
                qber = 0.5 // Conform specificației
                compromised = true
            } else {
                // Fără atac: QBER normal
                qber = Random.nextDouble(0.05, 0.15)
                compromised = false
            }
        }

        // Calculează scoruri folosind SecurityScorer
        val quantumScore = SecurityScorer.computeQuantumScore(
            keySize = keySize,
            qber = if (isQuantum) qber else null,
            eveDetected = eveDetected,
            scenario = scenario
        )
        
        val normalScore = SecurityScorer.computeNormalScore(
            keySize = keySize,
            qber = if (!isQuantum && shouldSimulateAttack) qber else null,
            compromised = compromised,
            scenario = scenario
        )

        val message = when {
            compromised -> "Tranzacție compromisă! Datele ar fi putut fi interceptate."
            eveDetected && isQuantum -> "Atac detectat și blocat. Datele sunt securizate."
            isQuantum -> "Tranzacție securizată cu QKD (Quantum Key Distribution)."
            else -> "Tranzacție procesată cu criptografie standard."
        }

        return TransactionResult(
            transactionId = UUID.randomUUID().toString(),
            scenario = request.scenario,
            mode = request.mode,
            normalScore = normalScore,
            quantumScore = quantumScore,
            qber = qber,
            eveDetected = eveDetected,
            compromised = compromised,
            success = !compromised,
            message = message
        )
    }

    private suspend fun cacheTransactionWithSecurityFields(
        transactionDto: TransactionDto,
        processingDto: TransactionProcessingDto,
        securityDto: SecurityAnalysisDto,
        userId: UUID,
        beneficiary: String
    ) {
        val resolvedId = transactionDto.transactionId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        val resolvedCreatedAt = transactionDto.createdAt?.let { Instant.parse(it) } ?: Instant.now()
        val amountVal = transactionDto.amount ?: 0.0
        val beneficiaryVal = transactionDto.beneficiaryName
            ?: (listOfNotNull(
                transactionDto.patientId?.let { "Patient: $it" },
                transactionDto.accessReason?.let { "Reason: $it" }
            ).joinToString(" | ").ifEmpty { null } ?: beneficiary)
        val entity = LocalTransactionEntity(
            transactionId = resolvedId,
            userId = userId,
            amount = amountVal,
            beneficiary = beneficiaryVal,
            mode = processingDto.processingMode,
            status = processingDto.status,
            intercepted = securityDto.eveDetected ?: false,
            lastUpdated = Instant.now(),
            createdAt = resolvedCreatedAt,
            scenario = transactionDto.scenario,
            securityScoreNormal = securityDto.securityScoreNormal,
            securityScoreQuantum = securityDto.securityScoreQuantum,
            qber = securityDto.qber,
            eveDetected = securityDto.eveDetected,
            compromised = securityDto.compromised,
            patientId = transactionDto.patientId,
            accessReason = transactionDto.accessReason
        )
        transactionDao.insert(entity)
    }

    override fun getSecurityScoreSummary(): SecurityScoreSummary {
        val transactions = getTransactionHistory()
        
        if (transactions.isEmpty()) {
            return SecurityScoreSummary(
                normalScore = 0,
                quantumScore = 0,
                transactionCount = 0,
                lastUpdated = "N/A"
            )
        }

        // Calculăm media scorurilor din ultimele N tranzacții
        val recentTransactions = transactions.take(10)
        val normalScores = recentTransactions.mapNotNull { it.securityScoreNormal }
        val quantumScores = recentTransactions.mapNotNull { it.securityScoreQuantum }

        val avgNormal = if (normalScores.isNotEmpty()) normalScores.average().toInt() else 65
        val avgQuantum = if (quantumScores.isNotEmpty()) quantumScores.average().toInt() else 92

        val lastUpdated = transactions.firstOrNull()?.dateTime ?: "N/A"

        return SecurityScoreSummary(
            normalScore = avgNormal,
            quantumScore = avgQuantum,
            transactionCount = transactions.size,
            lastUpdated = lastUpdated
        )
    }

    override fun getComparisonTimelineSteps(): List<ComparisonTimelineStep> {
        // Standard steps for Normal vs Quantum comparison
        val lastTransaction = getLastTransaction()
        val wasCompromised = lastTransaction?.compromised == true
        val wasEveDetected = lastTransaction?.eveDetected == true

        return listOf(
            ComparisonTimelineStep(
                stepName = "Connection Initiation",
                normalStatus = TimelineStepStatus.OK,
                quantumStatus = TimelineStepStatus.OK,
                normalDetail = "Standard TLS connection",
                quantumDetail = "Quantum channel established"
            ),
            ComparisonTimelineStep(
                stepName = "Key Exchange",
                normalStatus = if (wasCompromised) TimelineStepStatus.WARNING else TimelineStepStatus.OK,
                quantumStatus = TimelineStepStatus.OK,
                normalDetail = if (wasCompromised) "RSA/ECC (vulnerable to quantum)" else "Standard RSA/ECC",
                quantumDetail = "QKD BB84 protocol"
            ),
            ComparisonTimelineStep(
                stepName = "Integrity Verification",
                normalStatus = if (wasCompromised) TimelineStepStatus.COMPROMISED else TimelineStepStatus.OK,
                quantumStatus = if (wasEveDetected) TimelineStepStatus.WARNING else TimelineStepStatus.OK,
                normalDetail = if (wasCompromised) "Cannot detect interceptions" else "Hash verified",
                quantumDetail = if (wasEveDetected) "Eve detected, key regenerated" else "QBER < 5%, no interceptions"
            ),
            ComparisonTimelineStep(
                stepName = "Data Encryption",
                normalStatus = if (wasCompromised) TimelineStepStatus.COMPROMISED else TimelineStepStatus.OK,
                quantumStatus = TimelineStepStatus.OK,
                normalDetail = if (wasCompromised) "Data potentially exposed" else "AES-256 applied",
                quantumDetail = "AES-256 + quantum key"
            ),
            ComparisonTimelineStep(
                stepName = "Transaction Completion",
                normalStatus = if (wasCompromised) TimelineStepStatus.COMPROMISED else TimelineStepStatus.OK,
                quantumStatus = TimelineStepStatus.OK,
                normalDetail = if (wasCompromised) "Compromised" else "Success",
                quantumDetail = "Success - maximum security"
            )
        )
    }

    override fun getLastTransaction(): TransactionHistoryEntry? {
        return getTransactionHistory().firstOrNull()
    }

    companion object {
        private const val TAG = "TransactionRepository"
    }
}
