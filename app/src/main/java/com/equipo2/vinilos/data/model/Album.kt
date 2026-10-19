package com.equipo2.vinilos.data.model

import java.time.LocalDate

data class Album(
    val id: Int,
    val name: String,
    val cover: String,
    val releaseDate: LocalDate,
    val description: String,
    val genre: String,
    val recordLabel: String,
)

/** Datos para crear un álbum, antes de que la API le asigne un id. */
data class NewAlbum(
    val name: String,
    val cover: String,
    val releaseDate: LocalDate,
    val description: String,
    val genre: String,
    val recordLabel: String,
)

/** Únicos valores que acepta BackVynils; cualquier otro responde 400. */
object AlbumCatalog {
    val genres = listOf("Classical", "Salsa", "Rock", "Folk")
    val recordLabels = listOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")
}
