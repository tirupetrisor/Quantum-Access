package com.example.quantumaccess.data.sample

import com.example.quantumaccess.data.repository.QuantumTransactionRepository
import com.example.quantumaccess.domain.repository.DeviceRepository
import com.example.quantumaccess.domain.repository.TransactionRepository

/**
 * Service Locator simplu pentru a furniza repository-uri în aplicație.
 * Este inițializat în QuantumAccessApplication.
 */
object RepositoryProvider {
    
    lateinit var transactionRepository: TransactionRepository
        private set
        
    lateinit var deviceRepository: DeviceRepository
        private set
    
    lateinit var quantumTransactionRepository: QuantumTransactionRepository
        private set

    fun initialize(
        transactionRepo: TransactionRepository,
        deviceRepo: DeviceRepository,
        quantumRepo: QuantumTransactionRepository
    ) {
        transactionRepository = transactionRepo
        deviceRepository = deviceRepo
        quantumTransactionRepository = quantumRepo
    }
}
