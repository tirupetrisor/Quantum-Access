package com.example.quantumaccess.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quantumaccess.data.local.entities.LocalUserEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * DAO pentru accesarea datelor utilizatorului.
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: LocalUserEntity)

    @Update
    suspend fun update(user: LocalUserEntity)

    @Query("SELECT * FROM local_users WHERE userId = :userId")
    suspend fun getById(userId: UUID): LocalUserEntity?

    /**
     * Returnează primul utilizator găsit (presupunând single-user app).
     */
    @Query("SELECT * FROM local_users LIMIT 1")
    fun getCurrentUser(): Flow<LocalUserEntity?>
    
    @Query("SELECT * FROM local_users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): LocalUserEntity?
}
