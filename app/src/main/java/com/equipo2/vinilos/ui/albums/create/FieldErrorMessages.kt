package com.equipo2.vinilos.ui.albums.create

import android.content.Context
import com.equipo2.vinilos.R
import com.equipo2.vinilos.domain.AlbumField
import com.equipo2.vinilos.domain.FieldError

/** El mensaje que se muestra debajo de cada campo (sección 10.1 de la guía). */
fun FieldError.message(context: Context, field: AlbumField): String = when (this) {
    FieldError.Required -> context.getString(
        when (field) {
            AlbumField.NAME -> R.string.error_album_name_required
            AlbumField.COVER -> R.string.error_cover_required
            AlbumField.RELEASE_DATE -> R.string.error_date_required
            AlbumField.GENRE -> R.string.error_genre_required
            AlbumField.RECORD_LABEL -> R.string.error_label_required
            AlbumField.DESCRIPTION -> R.string.error_description_required
        }
    )
    FieldError.InvalidUrl -> context.getString(R.string.error_cover_invalid)
    FieldError.FutureDate -> context.getString(R.string.error_date_future)
    is FieldError.TooLong -> context.getString(R.string.error_max_length, max)
}
