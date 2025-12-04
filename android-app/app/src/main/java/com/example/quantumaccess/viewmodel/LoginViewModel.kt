package com.example.quantumaccess.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantumaccess.app.QuantumAccessApplication
import com.example.quantumaccess.core.network.SupabaseClientProvider
import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.repository.AuthRepository
import com.example.quantumaccess.data.sample.RepositoryProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.quantumaccess.domain.repository.TransactionRepository
import java.util.UUID

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val quantumApp = app as QuantumAccessApplication
    private val repo = AuthRepository(
        prefs = SecurePrefsManager(app),
        userDao = quantumApp.database.userDao(),
        transactionDao = quantumApp.database.transactionDao(),
        sessionDao = quantumApp.database.sessionDao(),
        supabase = SupabaseClientProvider.client
    )
    private val transactionRepository: TransactionRepository = RepositoryProvider.transactionRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    sealed interface LoginEvent {
        data object Success : LoginEvent
        data class Error(val message: String) : LoginEvent
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repo.login(email, pass)
            
            _uiState.update { it.copy(isLoading = false) }
            
            result.fold(
                onSuccess = {
                    syncTransactionsForCurrentUser()
                    _events.send(LoginEvent.Success)
                },
                onFailure = { err -> 
                    val msg = err.message ?: "Login failed"
                    _uiState.update { it.copy(error = msg) }
                    _events.send(LoginEvent.Error(msg))
                }
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repo.logout()
            onComplete()
        }
    }

    fun onGoogleSignInSuccess(idToken: String?) {
        viewModelScope.launch {
            if (idToken == null) {
                val msg = "Google Sign-In failed: Missing ID Token"
                _uiState.update { it.copy(error = msg) }
                _events.send(LoginEvent.Error(msg))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repo.googleSignIn(idToken)
            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    syncTransactionsForCurrentUser()
                    _events.send(LoginEvent.Success)
                },
                onFailure = { err ->
                    val msg = err.message ?: "Google Sign-In failed"
                    _uiState.update { it.copy(error = msg) }
                    _events.send(LoginEvent.Error(msg))
                }
            )
        }
    }

    fun onBiometricAuthenticated() {
        viewModelScope.launch {
            // 1. Validate Local Cache
            val localUser = repo.getLocalUser()
            if (localUser == null) {
                val msg = "No saved account found. Please login first."
                _uiState.update { it.copy(error = msg) }
                _events.send(LoginEvent.Error(msg))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            // 2. Refresh Session Heartbeats
            repo.refreshSessionHeartbeat()
            val sessionRefreshed = repo.refreshSupabaseSessionIfNeeded()
            
            // 3. Sync Transactions (Best Effort)
            if (sessionRefreshed) {
                syncTransactionsSafe(localUser.userId)
            }

            _uiState.update { it.copy(isLoading = false) }
            _events.send(LoginEvent.Success)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun syncTransactionsForCurrentUser() {
        val user = repo.getCurrentUser()
        if (user != null) {
            syncTransactionsSafe(user.userId)
        }
    }

    private suspend fun syncTransactionsSafe(userId: UUID) {
        runCatching { 
            transactionRepository.syncTransactions(userId) 
        }
    }
}

