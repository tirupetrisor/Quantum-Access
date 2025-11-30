package com.example.quantumaccess.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Entitate singleton pentru date analitice agregate.
 * PK este fixat la 1 pentru a asigura o singură intrare.
 */
@Entity(tableName = "analytics")
data class AnalyticsEntity(
    @PrimaryKey
    val id: Int = 1,
    val quantumSuccess: Int,
    val quantumIntercepted: Int,
    val normalCount: Int,
    val lastUpdated: Instant
)

