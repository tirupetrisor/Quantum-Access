package com.example.quantumaccess.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantumaccess.app.QuantumAccessApplication
import com.example.quantumaccess.core.network.SupabaseClientProvider
import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.repository.AuthRepository
import com.example.quantumaccess.data.repository.RegistrationException
import com.example.quantumaccess.data.sample.RepositoryProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val emailError: String? = null,
    val generalError: String? = null
)

class RegisterViewModel(app: Application) : AndroidViewModel(app) {
    
    private val quantumApp = app as QuantumAccessApplication
    private val deviceRepo = quantumApp.deviceRepository
    
    private val repo = AuthRepository(
        prefs = SecurePrefsManager(app),
        userDao = quantumApp.database.userDao(),
        transactionDao = quantumApp.database.transactionDao(),
        sessionDao = quantumApp.database.sessionDao(),
        supabase = SupabaseClientProvider.client
    )

    sealed interface Event {
        data object Success : Event
        data class BiometricUpdated(val enabled: Boolean) : Event
        data class Error(val message: String) : Event
    }

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun clearErrors() {
        _uiState.update { it.copy(usernameError = null, emailError = null, generalError = null) }
    }

    fun register(fullName: String, username: String, email: String, password: String, biometricEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, usernameError = null, emailError = null, generalError = null) }
            val deviceId = deviceRepo.getDeviceId() 
            
            // Note: DeviceID logic is not enforced by remote DB yet, but passed for consistency
            val result = repo.register(fullName, username, email, password, deviceId, biometricEnabled)
            
            _uiState.update { it.copy(isLoading = false) }
            
            result.fold(
                onSuccess = {
                    val user = repo.getCurrentUser()
                    if (user != null) {
                         try {
                             RepositoryProvider.transactionRepository.syncTransactions(user.userId)
                         } catch (e: Exception) {
                             // Ignore sync error during reg
                         }
                    }
                    _events.send(Event.Success) 
                },
                onFailure = { error ->
                    when (error) {
                        is RegistrationException -> handleRegistrationException(error)
                        else -> handleGenericRegistrationError(error)
                    }
                }
            )
        }
    }

    fun onGoogleSignInSuccess(email: String, name: String, googleId: String, biometricEnabled: Boolean, idToken: String?) {
        viewModelScope.launch {
            if (idToken != null) {
                _uiState.update { it.copy(isLoading = true) }
                val result = repo.googleSignIn(idToken, biometricEnabled = biometricEnabled)
                _uiState.update { it.copy(isLoading = false) }
                
                result.fold(
                    onSuccess = {
                         val user = repo.getCurrentUser()
                         if (user != null) {
                             try {
                                 RepositoryProvider.transactionRepository.syncTransactions(user.userId)
                             } catch (e: Exception) {
                                 // Ignore sync error
                             }
                         }
                        _events.send(Event.Success)
                    },
                    onFailure = { error -> 
                        _uiState.update { it.copy(generalError = error.message ?: "Google Sign-In failed") }
                        _events.send(Event.Error(error.message ?: "Google Sign-In failed"))
                    }
                )
            } else {
                _events.send(Event.Error("Google Sign-In failed: Missing ID Token"))
            }
        }
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun onGoogleSignInClick(biometricEnabled: Boolean) {
       // Trigger native flow
    }

    fun enableBiometric(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val result = repo.updateBiometricStatus(enabled)
            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { _events.send(Event.BiometricUpdated(enabled)) },
                onFailure = { error ->
                    val message = error.message ?: "Failed to update biometric preference"
                    _uiState.update { it.copy(generalError = message) }
                    _events.send(Event.Error(message))
                }
            )
        }
    }

    private suspend fun handleRegistrationException(error: RegistrationException) {
        val message = error.message ?: "Registration failed"
        when (error.type) {
            RegistrationException.Type.USERNAME_TAKEN -> {
                _uiState.update { it.copy(usernameError = message) }
            }
            RegistrationException.Type.EMAIL_TAKEN -> {
                _uiState.update { it.copy(emailError = message) }
            }
            RegistrationException.Type.CONFIRMATION_REQUIRED -> {
                _uiState.update { it.copy(generalError = message) }
                _events.send(Event.Error(message))
            }
            else -> {
                _uiState.update { it.copy(generalError = message) }
                _events.send(Event.Error(message))
            }
        }
    }

    private suspend fun handleGenericRegistrationError(error: Throwable) {
        val message = error.message ?: "Registration failed"
        _uiState.update { it.copy(generalError = message) }
        _events.send(Event.Error(message))
    }
}
