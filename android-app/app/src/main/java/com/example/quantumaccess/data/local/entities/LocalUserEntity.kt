package com.example.quantumaccess.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entitate Room pentru utilizatorul local.
 * Stochează preferințele de bază legate de identitate.
 */
@Entity(tableName = "local_users")
data class LocalUserEntity(
    @PrimaryKey
    val userId: UUID,
    val username: String,
    val name: String,
    val biometricEnabled: Boolean
)

