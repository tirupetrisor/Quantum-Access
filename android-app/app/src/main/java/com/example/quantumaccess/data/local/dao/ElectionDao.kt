package com.example.quantumaccess.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quantumaccess.data.local.entities.ElectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(elections: List<ElectionEntity>)

    @Query("SELECT * FROM elections WHERE isActive = 1 ORDER BY endTimeMillis DESC")
    fun getAllActiveFlow(): Flow<List<ElectionEntity>>

    @Query("SELECT * FROM elections ORDER BY endTimeMillis DESC")
    fun getAllFlow(): Flow<List<ElectionEntity>>

    @Query("SELECT * FROM elections WHERE id = :electionId")
    suspend fun getById(electionId: String): ElectionEntity?

    @Query("SELECT COUNT(*) FROM elections")
    suspend fun count(): Int
}
