package com.example.quantumaccess.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quantumaccess.data.local.entities.AnalyticsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO pentru analytics.
 */
@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics WHERE id = 1")
    fun getAnalyticsFlow(): Flow<AnalyticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AnalyticsEntity)

    @Update
    suspend fun updateAnalytics(analytics: AnalyticsEntity)
    
    // Helper to initialize if missing
    @Query("INSERT OR IGNORE INTO analytics (id, quantumSuccess, quantumIntercepted, normalCount, lastUpdated) VALUES (1, 0, 0, 0, 0)")
    suspend fun initAnalytics()
}

