package com.example.quantumaccess.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quantumaccess.data.local.entities.VoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vote: VoteEntity)

    @Query("SELECT * FROM votes ORDER BY createdAtMillis DESC")
    fun getAllFlow(): Flow<List<VoteEntity>>

    @Query("SELECT * FROM votes WHERE electionId = :electionId ORDER BY createdAtMillis DESC")
    fun getByElectionFlow(electionId: String): Flow<List<VoteEntity>>

    @Query("SELECT * FROM votes WHERE id = :voteId")
    suspend fun getById(voteId: String): VoteEntity?

    @Query("SELECT COUNT(*) FROM votes WHERE electionId = :electionId")
    suspend fun countByElection(electionId: String): Int
}
