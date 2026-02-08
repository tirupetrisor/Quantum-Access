package com.example.quantumaccess.data.repository

import android.content.Context
import com.example.quantumaccess.BuildConfig
import com.example.quantumaccess.data.local.dao.ElectionDao
import com.example.quantumaccess.data.local.dao.VoteDao
import com.example.quantumaccess.data.local.entities.ElectionEntity
import com.example.quantumaccess.data.local.entities.VoteEntity
import com.example.quantumaccess.data.local.mappers.toDomainModel
import com.example.quantumaccess.data.quantum.EveDetector
import com.example.quantumaccess.data.quantum.QKDProvider
import com.example.quantumaccess.data.quantum.QKDService
import com.example.quantumaccess.domain.model.Election
import com.example.quantumaccess.domain.model.VoteReceipt
import com.example.quantumaccess.domain.repository.VoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Vote repository: QKD key generation + Eve detection before accepting a vote.
 * If Eve is detected, vote is not cast. Otherwise vote is encrypted (conceptually) with the quantum key and stored.
 */
class VoteRepositoryImpl(
    private val context: Context,
    private val electionDao: ElectionDao,
    private val voteDao: VoteDao
) : VoteRepository {

    private val qkdService = QKDService(
        apiKey = BuildConfig.QKD_API_KEY.ifBlank { "simulation" },
        provider = when (BuildConfig.QKD_PROVIDER) {
            "QRYPT" -> QKDProvider.QRYPT
            "QBITSHIELD" -> QKDProvider.QBITSHIELD
            else -> QKDProvider.SIMULATION
        }
    )
    private val eveDetector = EveDetector()

    override fun getActiveElections(): Flow<List<Election>> {
        return electionDao.getAllActiveFlow().map { list ->
            list.map { it.toDomainModel(System.currentTimeMillis()) }
        }
    }

    override suspend fun getElection(electionId: String): Election? {
        val entity = electionDao.getById(electionId) ?: return null
        return entity.toDomainModel(System.currentTimeMillis())
    }

    override fun getVoteHistory(): Flow<List<com.example.quantumaccess.domain.model.Vote>> {
        return voteDao.getAllFlow().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun castVote(
        electionId: String,
        optionId: String,
        optionLabel: String,
        electionName: String,
        simulateEve: Boolean
    ): Result<VoteReceipt> {
        val voteId = UUID.randomUUID().toString()
        val transactionId = "vote-$electionId-$voteId"

        val keyResult = qkdService.generateQuantumKey(keySize = 256, transactionId = transactionId)
        val keyData = keyResult.getOrElse {
            return Result.failure(it)
        }

        val detection = eveDetector.detectEavesdropping(
            quantumEntropy = keyData.quantumEntropy,
            keySize = keyData.keySize,
            isEveEnabled = simulateEve
        )

        if (detection.isIntercepted) {
            return Result.failure(
                SecurityException("Eavesdropping detected (QBER: ${(detection.qber * 100).toInt()}%). Vote aborted for your security.")
            )
        }

        val receiptToken = "#QV-${randomHex(8)}"
        val encryptedPayload = "QKD:${keyData.keyId}:${keyData.keyMaterial.take(16)}" // Conceptual; production would use AES-GCM

        val entity = VoteEntity(
            id = voteId,
            electionId = electionId,
            electionName = electionName,
            optionId = optionId,
            optionLabel = optionLabel,
            encryptedPayload = encryptedPayload,
            quantumKeyId = keyData.keyId,
            receiptToken = receiptToken,
            createdAtMillis = System.currentTimeMillis(),
            isRealQkd = keyData.isReal,
            eveDetected = false
        )
        voteDao.insert(entity)

        return Result.success(
            VoteReceipt(
                voteId = voteId,
                electionName = electionName,
                receiptToken = receiptToken,
                createdAtMillis = entity.createdAtMillis,
                quantumSecured = keyData.isReal
            )
        )
    }

    override suspend fun seedElectionsIfNeeded() {
        if (electionDao.count() > 0) return
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L
        val elections = listOf(
            ElectionEntity(
                id = "pres-2024",
                type = "PRESIDENTIAL",
                name = "Presidential Election 2024",
                nameRo = "Alegeri Prezidențiale 2024",
                startTimeMillis = now - 30 * day,
                endTimeMillis = now + 60 * day,
                optionsJson = """[{"id":"cand-a","label":"Maria Popescu","shortLabel":"Popescu"},{"id":"cand-b","label":"Ion Ionescu","shortLabel":"Ionescu"},{"id":"cand-c","label":"Ana Maria Vasilescu","shortLabel":"Vasilescu"}]""",
                isActive = true
            ),
            ElectionEntity(
                id = "parl-2024",
                type = "PARLIAMENTARY",
                name = "Parliamentary Election 2024",
                nameRo = "Alegeri Parlamentare 2024",
                startTimeMillis = now - 15 * day,
                endTimeMillis = now + 90 * day,
                optionsJson = """[{"id":"party-x","label":"Partidul Verde","shortLabel":"Verde"},{"id":"party-y","label":"Alianța pentru Dezvoltare","shortLabel":"APD"},{"id":"party-z","label":"Mișcarea Civică","shortLabel":"MC"}]""",
                isActive = true
            ),
            ElectionEntity(
                id = "local-2024",
                type = "LOCAL",
                name = "Local Elections 2024",
                nameRo = "Alegeri Locale 2024",
                startTimeMillis = now - 7 * day,
                endTimeMillis = now + 30 * day,
                optionsJson = """[{"id":"mayor-1","label":"Primar Sector 1 - Lista A","shortLabel":"Lista A"},{"id":"mayor-2","label":"Primar Sector 1 - Lista B","shortLabel":"Lista B"}]""",
                isActive = true
            )
        )
        electionDao.insertAll(elections)
    }

    private fun randomHex(length: Int): String {
        val chars = "0123456789ABCDEF"
        return (1..length).map { chars.random() }.joinToString("")
    }
}
