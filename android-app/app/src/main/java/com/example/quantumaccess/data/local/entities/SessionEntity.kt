package com.example.quantumaccess.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

/**
 * Entitate pentru sesiunea activă.
 * Gestionează token-ul și expirarea acestuia.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val token: String,
    val userId: UUID,
    val expiresAt: Instant
)

