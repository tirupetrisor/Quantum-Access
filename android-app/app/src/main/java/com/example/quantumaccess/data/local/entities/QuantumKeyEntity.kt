package com.example.quantumaccess.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

/**
 * Local storage for quantum keys generated via QKD
 */
@Entity(
    tableName = "quantum_keys",
    foreignKeys = [
        ForeignKey(
            entity = LocalTransactionEntity::class,
            parentColumns = ["transactionId"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["generated_at"]),
        Index(value = ["is_real"])
    ]
)
data class QuantumKeyEntity(
    @PrimaryKey
    @ColumnInfo(name = "key_id")
    val keyId: String,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: UUID,
    
    @ColumnInfo(name = "key_material_hash")
    val keyMaterialHash: String, // Store hash, not raw key for security
    
    @ColumnInfo(name = "key_size")
    val keySize: Int,
    
    @ColumnInfo(name = "algorithm")
    val algorithm: String,
    
    @ColumnInfo(name = "provider")
    val provider: String,
    
    @ColumnInfo(name = "generated_at")
    val generatedAt: Instant,
    
    @ColumnInfo(name = "quantum_entropy")
    val quantumEntropy: Double,
    
    @ColumnInfo(name = "is_real")
    val isReal: Boolean, // True if from real QKD, false if simulated
    
    @ColumnInfo(name = "used_at")
    val usedAt: Instant? = null
)
