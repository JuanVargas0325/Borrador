package com.equipo2.vinilos.ui.albums.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.equipo2.vinilos.domain.AlbumField
import com.equipo2.vinilos.domain.AlbumForm
import com.equipo2.vinilos.domain.AlbumFormValidator
import com.equipo2.vinilos.domain.FieldError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class AlbumCreateUiState(
    val form: AlbumForm = AlbumForm(),
    val errors: Map<AlbumField, FieldError> = emptyMap(),
)

sealed interface AlbumCreateEvent {
    /** Guardar con errores: la pantalla lleva el foco al primer campo con error. */
    data class ShowErrors(val firstField: AlbumField) : AlbumCreateEvent
}

class AlbumCreateViewModel(
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumCreateUiState())
    val state: StateFlow<AlbumCreateUiState> = _state.asStateFlow()

    private val _events = Channel<AlbumCreateEvent>(Channel.BUFFERED)
    val events: Flow<AlbumCreateEvent> = _events.receiveAsFlow()

    /** Después del primer intento de guardar, cada cambio vuelve a validar el formulario. */
    private var validateOnChange = false

    fun onNameChange(value: String) = edit { copy(name = value) }
    fun onCoverChange(value: String) = edit { copy(cover = value) }
    fun onReleaseDateChange(value: LocalDate) = edit { copy(releaseDate = value) }
    fun onGenreChange(value: String) = edit { copy(genre = value) }
    fun onRecordLabelChange(value: String) = edit { copy(recordLabel = value) }
    fun onDescriptionChange(value: String) = edit { copy(description = value) }

    fun save() {
        validateOnChange = true
        val errors = AlbumFormValidator.validate(_state.value.form, today())
        _state.update { it.copy(errors = errors) }
        errors.keys.firstOrNull()?.let { _events.trySend(AlbumCreateEvent.ShowErrors(it)) }
    }

    private fun edit(change: AlbumForm.() -> AlbumForm) = _state.update { current ->
        val form = current.form.change()
        val errors = if (validateOnChange) AlbumFormValidator.validate(form, today()) else current.errors
        current.copy(form = form, errors = errors)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AlbumCreateViewModel() }
        }
    }
}
