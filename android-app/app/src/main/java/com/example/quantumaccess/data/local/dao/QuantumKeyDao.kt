package com.example.quantumaccess.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quantumaccess.data.local.entities.QuantumKeyEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface QuantumKeyDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: QuantumKeyEntity)
    
    @Query("SELECT * FROM quantum_keys WHERE key_id = :keyId")
    suspend fun getById(keyId: String): QuantumKeyEntity?
    
    @Query("SELECT * FROM quantum_keys WHERE transaction_id = :transactionId")
    suspend fun getByTransactionId(transactionId: UUID): QuantumKeyEntity?
    
    @Query("SELECT * FROM quantum_keys WHERE transaction_id = :transactionId")
    fun observeByTransactionId(transactionId: UUID): Flow<QuantumKeyEntity?>
    
    @Query("SELECT * FROM quantum_keys WHERE is_real = 1 ORDER BY generated_at DESC")
    fun observeRealQuantumKeys(): Flow<List<QuantumKeyEntity>>
    
    @Query("SELECT COUNT(*) FROM quantum_keys WHERE is_real = 1")
    suspend fun getRealKeyCount(): Int
    
    @Query("SELECT AVG(quantum_entropy) FROM quantum_keys WHERE is_real = 1")
    suspend fun getAverageQuantumEntropy(): Double?
    
    @Query("DELETE FROM quantum_keys WHERE key_id = :keyId")
    suspend fun deleteById(keyId: String)
    
    @Query("DELETE FROM quantum_keys")
    suspend fun deleteAll()
}
