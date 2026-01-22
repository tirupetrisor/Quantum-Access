package com.example.quantumaccess.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

/**
 * Entitate pentru tranzacții locale.
 * Stochează istoricul tranzacțiilor și starea lor de securitate.
 */
@Entity(tableName = "local_transactions")
data class LocalTransactionEntity(
    @PrimaryKey
    val transactionId: UUID,
    val userId: UUID,
    val amount: Double,
    val beneficiary: String = "Unknown", // Default for legacy rows
    val mode: String, // e.g., "QUANTUM", "NORMAL"
    val status: String, // e.g., "SUCCESS", "FAILED", "INTERCEPTED"
    val intercepted: Boolean,
    val lastUpdated: Instant = Instant.now(),
    val createdAt: Instant,
    // New security fields
    val scenario: String? = null, // "BANKING_PAYMENT" | "MEDICAL_RECORD_ACCESS"
    val securityScoreNormal: Int? = null, // 0-100
    val securityScoreQuantum: Int? = null, // 0-100
    val qber: Double? = null, // Quantum Bit Error Rate
    val eveDetected: Boolean? = null, // Eavesdropper detected
    val compromised: Boolean? = null, // Transaction compromised
    val patientId: String? = null, // ID Pacient/CNP – doar pentru MEDICAL_RECORD_ACCESS
    val accessReason: String? = null // Motiv acces – doar pentru MEDICAL_RECORD_ACCESS
)
