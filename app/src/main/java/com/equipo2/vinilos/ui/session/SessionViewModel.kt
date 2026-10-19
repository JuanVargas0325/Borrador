package com.equipo2.vinilos.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.equipo2.vinilos.ServiceLocator
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.data.model.ThemeMode
import com.equipo2.vinilos.data.repository.PreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface SessionEvent {
    data class RoleChanged(val role: Role) : SessionEvent
}

/** Rol y tema de la sesión. Lo comparten la actividad y todas las pantallas. */
class SessionViewModel(private val preferences: PreferencesRepository) : ViewModel() {

    private val _role = MutableStateFlow<Role?>(null)
    val role: StateFlow<Role?> = _role.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _events = Channel<SessionEvent>(Channel.BUFFERED)
    val events: Flow<SessionEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch { preferences.role.collect { _role.value = it } }
        viewModelScope.launch { preferences.themeMode.collect { _themeMode.value = it } }
    }

    /** Desde la selección de rol: guarda el rol con el que se entra a la app. */
    fun enterAs(role: Role) {
        _role.value = role
        viewModelScope.launch { preferences.setRole(role) }
    }

    /** Desde el menú de rol: cambia el rol sin salir de la pantalla y avisa del cambio. */
    fun changeRole(role: Role) {
        if (_role.value == role) return
        _role.value = role
        viewModelScope.launch {
            preferences.setRole(role)
            _events.send(SessionEvent.RoleChanged(role))
        }
    }

    fun setDarkMode(enabled: Boolean) {
        val mode = if (enabled) ThemeMode.DARK else ThemeMode.LIGHT
        _themeMode.value = mode
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SessionViewModel(ServiceLocator.preferencesRepository) }
        }
    }
}
