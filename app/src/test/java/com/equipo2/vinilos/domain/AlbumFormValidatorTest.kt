package com.equipo2.vinilos.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AlbumFormValidatorTest {

    private val today = LocalDate.of(2026, 10, 18)
    private val validForm = AlbumForm(
        name = "Buscando América",
        cover = "https://ejemplo.com/portada.jpg",
        releaseDate = LocalDate.of(1984, 8, 1),
        genre = "Salsa",
        recordLabel = "Elektra",
        description = "Álbum de Rubén Blades.",
    )

    @Test
    fun formularioValido_noTieneErrores() {
        assertTrue(AlbumFormValidator.validate(validForm, today).isEmpty())
    }

    @Test
    fun formularioVacio_todosLosCamposSonObligatorios() {
        val errors = AlbumFormValidator.validate(AlbumForm(), today)
        assertEquals(AlbumField.entries.toSet(), errors.keys)
        assertTrue(errors.values.all { it == FieldError.Required })
    }

    @Test
    fun soloEspacios_cuentaComoVacio() {
        val errors = AlbumFormValidator.validate(validForm.copy(name = "   ", description = " "), today)
        assertEquals(FieldError.Required, errors[AlbumField.NAME])
        assertEquals(FieldError.Required, errors[AlbumField.DESCRIPTION])
    }

    @Test
    fun urlSinProtocolo_esInvalida() {
        val errors = AlbumFormValidator.validate(validForm.copy(cover = "ejemplo.com/portada.jpg"), today)
        assertEquals(FieldError.InvalidUrl, errors[AlbumField.COVER])
    }

    @Test
    fun formatosDeUrl() {
        assertTrue(AlbumFormValidator.isValidUrl("http://a.co/x.png"))
        assertTrue(AlbumFormValidator.isValidUrl("HTTPS://EJEMPLO.COM/A.JPG"))
        assertFalse(AlbumFormValidator.isValidUrl("https://sinpunto"))
        assertFalse(AlbumFormValidator.isValidUrl("ftp://ejemplo.com/a.jpg"))
        assertFalse(AlbumFormValidator.isValidUrl("https://ejemplo .com/a.jpg"))
    }

    @Test
    fun fechaFutura_esInvalida_yHoyEsValida() {
        val future = AlbumFormValidator.validate(validForm.copy(releaseDate = today.plusDays(1)), today)
        assertEquals(FieldError.FutureDate, future[AlbumField.RELEASE_DATE])
        assertTrue(AlbumFormValidator.validate(validForm.copy(releaseDate = today), today).isEmpty())
    }

    @Test
    fun generoOSelloQueLaApiNoAcepta_sonInvalidos() {
        val errors = AlbumFormValidator.validate(validForm.copy(genre = "Pop", recordLabel = "Otro"), today)
        assertEquals(FieldError.Required, errors[AlbumField.GENRE])
        assertEquals(FieldError.Required, errors[AlbumField.RECORD_LABEL])
    }

    @Test
    fun longitudesMaximas() {
        val errors = AlbumFormValidator.validate(validForm.copy(name = "a".repeat(101), description = "b".repeat(1001)), today)
        assertEquals(FieldError.TooLong(AlbumFormValidator.NAME_MAX_LENGTH), errors[AlbumField.NAME])
        assertEquals(FieldError.TooLong(AlbumFormValidator.DESCRIPTION_MAX_LENGTH), errors[AlbumField.DESCRIPTION])
        assertTrue(AlbumFormValidator.validate(validForm.copy(name = "a".repeat(100)), today).isEmpty())
    }

    @Test
    fun losErroresVanEnElOrdenDelFormulario() {
        val errors = AlbumFormValidator.validate(validForm.copy(cover = "", description = ""), today)
        assertEquals(listOf(AlbumField.COVER, AlbumField.DESCRIPTION), errors.keys.toList())
    }

    @Test
    fun alConvertirParaLaApi_seRecortanLosEspacios() {
        val album = validForm.copy(name = "  Siembra  ", cover = " https://a.co/b.jpg ").toNewAlbum()
        assertEquals("Siembra", album.name)
        assertEquals("https://a.co/b.jpg", album.cover)
    }
}
