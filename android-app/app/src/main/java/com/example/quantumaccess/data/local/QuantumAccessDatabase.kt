package com.example.quantumaccess.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quantumaccess.data.local.dao.SessionDao
import com.example.quantumaccess.data.local.dao.TransactionDao
import com.example.quantumaccess.data.local.dao.UserDao
import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import com.example.quantumaccess.data.local.entities.LocalUserEntity
import com.example.quantumaccess.data.local.entities.SessionEntity

/**
 * Baza de date principală Room pentru QuantumAccess.
 * Definește entitățile, versiunea și convertoarele de tip.
 */
@Database(
    entities = [
        LocalUserEntity::class,
        SessionEntity::class,
        LocalTransactionEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(com.example.quantumaccess.data.local.TypeConverters::class)
abstract class QuantumAccessDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun transactionDao(): TransactionDao
    companion object {
        @Volatile
        private var INSTANCE: QuantumAccessDatabase? = null

        fun getInstance(context: Context): QuantumAccessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuantumAccessDatabase::class.java,
                    "quantum_access_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
