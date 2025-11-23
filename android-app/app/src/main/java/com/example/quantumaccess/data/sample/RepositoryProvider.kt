package com.example.quantumaccess.data.sample

import com.example.quantumaccess.data.repository.HardcodedTransactionRepository
import com.example.quantumaccess.domain.repository.TransactionRepository

object RepositoryProvider {
    val transactionRepository: TransactionRepository by lazy { HardcodedTransactionRepository() }
}

