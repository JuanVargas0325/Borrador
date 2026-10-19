package com.equipo2.vinilos.domain

import com.equipo2.vinilos.data.model.AlbumCatalog
import com.equipo2.vinilos.data.model.NewAlbum
import java.time.LocalDate

/** Lo que el usuario lleva escrito en el formulario de crear álbum. */
data class AlbumForm(
    val name: String = "",
    val cover: String = "",
    val releaseDate: LocalDate? = null,
    val genre: String? = null,
    val recordLabel: String? = null,
    val description: String = "",
) {
    val isEmpty: Boolean
        get() = name.isBlank() && cover.isBlank() && releaseDate == null &&
            genre == null && recordLabel == null && description.isBlank()

    /** Solo se llama con un formulario válido: todos los campos tienen valor. */
    fun toNewAlbum() = NewAlbum(
        name = name.trim(),
        cover = cover.trim(),
        releaseDate = checkNotNull(releaseDate),
        description = description.trim(),
        genre = checkNotNull(genre),
        recordLabel = checkNotNull(recordLabel),
    )
}

/** Campos del formulario, en el orden en que aparecen en pantalla. */
enum class AlbumField { NAME, COVER, RELEASE_DATE, GENRE, RECORD_LABEL, DESCRIPTION }

sealed interface FieldError {
    data object Required : FieldError
    data object InvalidUrl : FieldError
    data object FutureDate : FieldError
    data class TooLong(val max: Int) : FieldError
}

/**
 * Reglas de la sección 10.1 de la guía. BackVynils solo exige que los campos existan, que la fecha sea válida y que
 * el género y el sello estén en su lista; la URL, la fecha futura y las longitudes las valida la app.
 */
object AlbumFormValidator {

    const val NAME_MAX_LENGTH = 100
    const val DESCRIPTION_MAX_LENGTH = 1000
    private val urlPattern = Regex("""^https?://[^\s/]+\.\S+$""", RegexOption.IGNORE_CASE)

    fun validate(form: AlbumForm, today: LocalDate): Map<AlbumField, FieldError> = buildMap {
        textError(form.name, NAME_MAX_LENGTH)?.let { put(AlbumField.NAME, it) }
        coverError(form.cover)?.let { put(AlbumField.COVER, it) }
        dateError(form.releaseDate, today)?.let { put(AlbumField.RELEASE_DATE, it) }
        if (form.genre !in AlbumCatalog.genres) put(AlbumField.GENRE, FieldError.Required)
        if (form.recordLabel !in AlbumCatalog.recordLabels) put(AlbumField.RECORD_LABEL, FieldError.Required)
        textError(form.description, DESCRIPTION_MAX_LENGTH)?.let { put(AlbumField.DESCRIPTION, it) }
    }

    fun isValidUrl(value: String): Boolean = urlPattern.matches(value.trim())

    private fun textError(value: String, maxLength: Int): FieldError? = when {
        value.isBlank() -> FieldError.Required
        value.trim().length > maxLength -> FieldError.TooLong(maxLength)
        else -> null
    }

    private fun coverError(value: String): FieldError? = when {
        value.isBlank() -> FieldError.Required
        !isValidUrl(value) -> FieldError.InvalidUrl
        else -> null
    }

    private fun dateError(value: LocalDate?, today: LocalDate): FieldError? = when {
        value == null -> FieldError.Required
        value.isAfter(today) -> FieldError.FutureDate
        else -> null
    }
}
