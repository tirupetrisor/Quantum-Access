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
    val createdAt: Instant
)
