package com.equipo2.vinilos.data.network

import com.equipo2.vinilos.data.model.Album
import com.equipo2.vinilos.data.model.NewAlbum
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/** Única puerta hacia BackVynils: oculta Retrofit y el JSON, y entrega modelos de la app. */
class NetworkServiceAdapter(baseUrl: String) {

    private val json = Json { ignoreUnknownKeys = true }

    // El backend vive en Render y la primera petición puede tardar mientras el servidor despierta.
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(VinilosApiService::class.java)

    suspend fun createAlbum(album: NewAlbum): Album =
        api.createAlbum(album.toRequest()).toAlbum()

    companion object {
        private const val TIMEOUT_SECONDS = 30L

        fun toApiDate(date: LocalDate): String = "${date}T00:00:00.000Z"

        fun fromApiDate(value: String): LocalDate = OffsetDateTime.parse(value).toLocalDate()
    }
}

private fun NewAlbum.toRequest() = CreateAlbumRequest(
    name = name,
    cover = cover,
    releaseDate = NetworkServiceAdapter.toApiDate(releaseDate),
    description = description,
    genre = genre,
    recordLabel = recordLabel,
)

private fun AlbumDto.toAlbum() = Album(
    id = id,
    name = name,
    cover = cover,
    releaseDate = NetworkServiceAdapter.fromApiDate(releaseDate),
    description = description,
    genre = genre,
    recordLabel = recordLabel,
)
