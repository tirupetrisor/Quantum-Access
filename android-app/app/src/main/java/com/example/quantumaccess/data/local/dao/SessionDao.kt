package com.example.quantumaccess.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quantumaccess.data.local.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO pentru gestionarea sesiunilor.
 */
@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    /**
     * Obține sesiunea curentă (ultima inserată sau validă).
     */
    @Query("SELECT * FROM sessions ORDER BY expiresAt DESC LIMIT 1")
    fun getSessionFlow(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions ORDER BY expiresAt DESC LIMIT 1")
    suspend fun getLatestSession(): SessionEntity?

    @Query("DELETE FROM sessions")
    suspend fun clearSession()
}

