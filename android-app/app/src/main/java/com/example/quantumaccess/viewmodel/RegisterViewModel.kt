package com.example.quantumaccess.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantumaccess.data.local.DeviceRepository
import com.example.quantumaccess.data.local.PreferencesManager
import com.example.quantumaccess.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel(app: Application) : AndroidViewModel(app) {
	private val prefs = PreferencesManager(app)
	private val deviceRepo = DeviceRepository(app, prefs)
	private val repo = AuthRepository(prefs = prefs, useMock = true)

	sealed interface Event {
		data object Success : Event
		data class Error(val message: String) : Event
	}

	private val _events = Channel<Event>(Channel.BUFFERED)
	val events = _events.receiveAsFlow()

	fun register(fullName: String, username: String, password: String, biometricEnabled: Boolean) {
		viewModelScope.launch {
			val deviceId = deviceRepo.getOrCreateDeviceId()
			val result = repo.register(fullName, username, password, deviceId, biometricEnabled)
			result.fold(
				onSuccess = { _events.send(Event.Success) },
				onFailure = { _events.send(Event.Error(it.message ?: "Registration failed")) }
			)
		}
	}
}



