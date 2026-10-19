package com.equipo2.vinilos.ui.albums.create

import com.equipo2.vinilos.FakeAlbumRepository
import com.equipo2.vinilos.MainDispatcherRule
import com.equipo2.vinilos.domain.AlbumField
import com.equipo2.vinilos.domain.FieldError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AlbumCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAlbumRepository()
    private val viewModel = AlbumCreateViewModel(repository) { LocalDate.of(2026, 10, 18) }

    private fun fillValidForm() {
        viewModel.onNameChange("Siembra")
        viewModel.onCoverChange("https://ejemplo.com/siembra.jpg")
        viewModel.onReleaseDateChange(LocalDate.of(1978, 9, 1))
        viewModel.onGenreChange("Salsa")
        viewModel.onRecordLabelChange("Fania Records")
        viewModel.onDescriptionChange("Álbum de Willie Colón y Rubén Blades.")
    }

    @Test
    fun antesDeGuardar_noSeMuestranErrores() {
        viewModel.onCoverChange("no-es-url")
        assertTrue(viewModel.state.value.errors.isEmpty())
    }

    @Test
    fun guardarVacio_muestraLosErrores_yNoLlamaALaApi() = runTest {
        viewModel.save()
        assertEquals(AlbumField.entries.size, viewModel.state.value.errors.size)
        assertEquals(AlbumCreateEvent.ShowErrors(AlbumField.NAME), viewModel.events.first())
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun despuesDeGuardar_losErroresSeActualizanAlEscribir() {
        viewModel.save()
        viewModel.onNameChange("Siembra")
        assertFalse(AlbumField.NAME in viewModel.state.value.errors)
        viewModel.onCoverChange("sin-protocolo")
        assertEquals(FieldError.InvalidUrl, viewModel.state.value.errors[AlbumField.COVER])
    }

    @Test
    fun guardarValido_creaElAlbum_yAvisa() = runTest {
        fillValidForm()
        viewModel.save()
        val event = viewModel.events.first()
        assertTrue(event is AlbumCreateEvent.Created)
        assertEquals("Siembra", (event as AlbumCreateEvent.Created).album.name)
        assertEquals(1, repository.created.size)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun siFallaElGuardado_conservaLosDatos_yAvisa() = runTest {
        repository.failCreate = true
        fillValidForm()
        viewModel.save()
        assertEquals(AlbumCreateEvent.SaveFailed, viewModel.events.first())
        assertEquals("Siembra", viewModel.state.value.form.name)
        assertEquals("Fania Records", viewModel.state.value.form.recordLabel)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun formularioVacio_permiteCerrarSinConfirmar() {
        assertTrue(viewModel.state.value.form.isEmpty)
        viewModel.onNameChange("x")
        assertFalse(viewModel.state.value.form.isEmpty)
    }
}
