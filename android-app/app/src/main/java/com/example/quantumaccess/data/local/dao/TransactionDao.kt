package com.example.quantumaccess.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * DAO pentru tranzacții.
 */
@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: LocalTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<LocalTransactionEntity>)

    @Query("SELECT * FROM local_transactions ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<LocalTransactionEntity>>

    @Query("SELECT * FROM local_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactions(userId: UUID): Flow<List<LocalTransactionEntity>>

    @Query("SELECT * FROM local_transactions WHERE transactionId = :id")
    suspend fun getById(id: UUID): LocalTransactionEntity?

    @Query("DELETE FROM local_transactions")
    suspend fun deleteAll()

    @Query("DELETE FROM local_transactions WHERE userId = :userId")
    suspend fun clearUserData(userId: UUID)
    
    @Query("SELECT COUNT(*) FROM local_transactions WHERE mode = :mode")
    suspend fun getCountByMode(mode: String): Int
}
