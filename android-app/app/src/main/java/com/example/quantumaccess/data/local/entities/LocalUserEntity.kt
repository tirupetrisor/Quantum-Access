package com.example.quantumaccess.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entitate Room pentru utilizatorul local.
 * Stochează preferințele de bază legate de identitate.
 */
@Entity(
    tableName = "local_users",
    indices = [
        androidx.room.Index(value = ["username"], unique = true),
        androidx.room.Index(value = ["email"], unique = true)
    ]
)
data class LocalUserEntity(
    @PrimaryKey
    val userId: UUID,
    val username: String,
    val email: String?,
    val name: String,
    val biometricEnabled: Boolean,
    val googleId: String? = null
)

