package com.example.quantumaccess.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a single cast vote.
 * encryptedPayload is the vote payload encrypted with the QKD key (for audit).
 */
@Entity(
    tableName = "votes",
    indices = [Index(value = ["electionId"]), Index(value = ["createdAtMillis"])]
)
data class VoteEntity(
    @PrimaryKey
    val id: String,
    val electionId: String,
    val electionName: String,
    val optionId: String,
    val optionLabel: String,
    val encryptedPayload: String,
    val quantumKeyId: String,
    val receiptToken: String,
    val createdAtMillis: Long,
    val isRealQkd: Boolean,
    val eveDetected: Boolean = false
)
