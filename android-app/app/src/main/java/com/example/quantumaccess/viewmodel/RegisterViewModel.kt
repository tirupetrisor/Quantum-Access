package com.example.quantumaccess.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantumaccess.app.QuantumAccessApplication
import com.example.quantumaccess.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel(app: Application) : AndroidViewModel(app) {
    
    // Use the centralized Manual DI Container from Application
    private val quantumApp = app as QuantumAccessApplication
    private val deviceRepo = quantumApp.deviceRepository
    
    // AuthRepository is currently created here because it's specific to auth logic
    // Ideally it should also be in the App container if shared, but creating it here is fine for now
    // reusing the dependencies from the App container.
    private val repo = AuthRepository(
        prefs = com.example.quantumaccess.data.local.SecurePrefsManager(app), // Or expose prefs from App
        userDao = quantumApp.database.userDao(), 
        useMock = true
    )

    sealed interface Event {
        data object Success : Event
        data class Error(val message: String) : Event
    }

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun register(fullName: String, username: String, password: String, biometricEnabled: Boolean) {
        viewModelScope.launch {
            val deviceId = deviceRepo.getDeviceId() 
            
            val result = repo.register(fullName, username, password, deviceId, biometricEnabled)
            result.fold(
                onSuccess = { _events.send(Event.Success) },
                onFailure = { _events.send(Event.Error(it.message ?: "Registration failed")) }
            )
        }
    }
}
