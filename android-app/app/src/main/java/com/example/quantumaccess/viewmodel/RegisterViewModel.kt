package com.example.quantumaccess.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantumaccess.app.QuantumAccessApplication
import com.example.quantumaccess.data.repository.AuthRepository
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
        prefs = com.example.quantumaccess.data.local.SecurePrefsManager(app),
        userDao = quantumApp.database.userDao(), 
        useMock = true
    )

    sealed interface Event {
        data object Success : Event
        // Păstrăm Error generic pentru Toast-uri dacă e cazul, dar preferăm state-ul pentru field errors
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
            
            val result = repo.register(fullName, username, email, password, deviceId, biometricEnabled)
            
            _uiState.update { it.copy(isLoading = false) }
            
            result.fold(
                onSuccess = { _events.send(Event.Success) },
                onFailure = { error -> 
                    val msg = error.message ?: "Registration failed"
                    if (msg.contains("Username-ul este deja utilizat", ignoreCase = true)) {
                        _uiState.update { it.copy(usernameError = msg) }
                    } else if (msg.contains("Există deja un cont cu acest email", ignoreCase = true)) {
                        _uiState.update { it.copy(emailError = msg) }
                    } else {
                        _uiState.update { it.copy(generalError = msg) }
                        _events.send(Event.Error(msg))
                    }
                }
            )
        }
    }

    fun onGoogleSignInSuccess(email: String, name: String, googleId: String, biometricEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, usernameError = null, emailError = null, generalError = null) }

            val result = repo.googleSignIn(email, name, googleId, biometricEnabled)
            
            _uiState.update { it.copy(isLoading = false) }
            
            result.fold(
                onSuccess = { _events.send(Event.Success) },
                onFailure = { 
                    val msg = it.message ?: "Google Sign-In failed"
                    _uiState.update { s -> s.copy(generalError = msg) }
                    _events.send(Event.Error(msg)) 
                }
            )
        }
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    // Legacy method kept for compatibility but should ideally be replaced
    fun onGoogleSignInClick(biometricEnabled: Boolean) {
        // This was the mock method. We'll redirect it to a failed state if called directly without data
        // or just log a warning. For now, let's leave it but it won't be used by the real UI flow.
    }
}
