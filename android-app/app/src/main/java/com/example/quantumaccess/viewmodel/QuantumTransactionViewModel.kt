package com.example.quantumaccess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quantumaccess.app.QuantumAccessApplication
import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.repository.QuantumTransactionRepository
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.QuantumProcessStep
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class QuantumTransactionViewModel(
    private val app: QuantumAccessApplication
) : ViewModel() {

    private val quantumRepo: QuantumTransactionRepository = 
        RepositoryProvider.quantumTransactionRepository

    /**
     * Process a quantum-secured transaction using real QKD
     */
    fun processQuantumTransaction(
        amount: Double,
        beneficiary: String,
        onProgress: suspend (QuantumProcessStep) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Get current user
                val userId = getCurrentUserId() ?: run {
                    onProgress(
                        QuantumProcessStep(
                            progress = 0f,
                            status = "Error",
                            detail = "User not logged in",
                            isTerminal = true
                        )
                    )
                    return@launch
                }

                // Process transaction
                val result = quantumRepo.processQuantumTransaction(
                    userId = userId,
                    amount = amount,
                    beneficiary = beneficiary,
                    onProgress = onProgress
                )

                if (result.isFailure) {
                    onProgress(
                        QuantumProcessStep(
                            progress = 0f,
                            status = "Transaction Failed",
                            detail = result.exceptionOrNull()?.message ?: "Unknown error",
                            isTerminal = true
                        )
                    )
                }
            } catch (e: Exception) {
                onProgress(
                    QuantumProcessStep(
                        progress = 0f,
                        status = "Error",
                        detail = e.message ?: "Unexpected error",
                        isTerminal = true
                    )
                )
            }
        }
    }

    private suspend fun getCurrentUserId(): UUID? = withContext(Dispatchers.IO) {
        try {
            // Try to get from Supabase session first
            val remoteId = com.example.quantumaccess.core.network.SupabaseClientProvider.client
                .auth.currentSessionOrNull()?.user?.id?.let { UUID.fromString(it) }
            
            if (remoteId != null) {
                return@withContext remoteId
            }
            
            // Fallback to local user
            app.database.userDao().getCurrentUser().first()?.userId
        } catch (e: Exception) {
            null
        }
    }

    class Factory(private val app: QuantumAccessApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuantumTransactionViewModel::class.java)) {
                return QuantumTransactionViewModel(app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
