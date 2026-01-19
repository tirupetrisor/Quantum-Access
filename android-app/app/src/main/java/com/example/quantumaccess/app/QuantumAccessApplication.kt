package com.example.quantumaccess.app

import android.app.Application
import com.example.quantumaccess.core.network.SupabaseClientProvider
import com.example.quantumaccess.data.local.QuantumAccessDatabase
import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.remote.RemoteTransactionDataSource
import com.example.quantumaccess.data.repository.DeviceRepositoryImpl
import com.example.quantumaccess.data.repository.QuantumTransactionRepository
import com.example.quantumaccess.data.repository.TransactionRepositoryImpl
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.repository.DeviceRepository
import com.example.quantumaccess.domain.repository.TransactionRepository

class QuantumAccessApplication : Application() {

    // Container for manual dependency injection
    lateinit var database: QuantumAccessDatabase
        private set

    lateinit var transactionRepository: TransactionRepository
        private set
        
    lateinit var deviceRepository: DeviceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Database
        database = QuantumAccessDatabase.getInstance(this)
        
        // 2. Initialize Remote Data Sources
        val supabaseClient = SupabaseClientProvider.client
        val remoteTransactionDS = RemoteTransactionDataSource(supabaseClient)
        
        // 3. Initialize Repositories
        val transactionDao = database.transactionDao()
        val quantumKeyDao = database.quantumKeyDao()
        val userDao = database.userDao()
        val prefs = SecurePrefsManager(this)
        
        transactionRepository = TransactionRepositoryImpl(
            context = this,
            transactionDao = transactionDao,
            userDao = userDao,
            remoteDataSource = remoteTransactionDS,
            supabase = supabaseClient
        )
        deviceRepository = DeviceRepositoryImpl(prefs, userDao)
        
        val quantumTransactionRepository = QuantumTransactionRepository(
            context = this,
            transactionDao = transactionDao,
            quantumKeyDao = quantumKeyDao,
            remoteDataSource = remoteTransactionDS,
            supabase = supabaseClient
        )

        // 4. Initialize Service Locator (Legacy bridge)
        RepositoryProvider.initialize(transactionRepository, deviceRepository, quantumTransactionRepository)
    }
}
