package com.example.quantumaccess.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for elections.
 * optionsJson: JSON array of {"id":"","label":""} for vote options.
 */
@Entity(tableName = "elections")
data class ElectionEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val name: String,
    val nameRo: String? = null,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val optionsJson: String,
    val isActive: Boolean = true
)
