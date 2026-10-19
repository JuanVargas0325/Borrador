package com.equipo2.vinilos

import com.equipo2.vinilos.data.model.Album
import com.equipo2.vinilos.data.model.NewAlbum
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.data.model.ThemeMode
import com.equipo2.vinilos.data.repository.AlbumRepository
import com.equipo2.vinilos.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/** Repositorio de álbumes en memoria: guarda lo que se le pide crear o falla a voluntad. */
class FakeAlbumRepository(var failCreate: Boolean = false) : AlbumRepository {

    val created = mutableListOf<NewAlbum>()

    override suspend fun createAlbum(album: NewAlbum): Result<Album> {
        if (failCreate) return Result.failure(IOException("Sin conexión"))
        created += album
        return Result.success(
            Album(
                id = created.size,
                name = album.name,
                cover = album.cover,
                releaseDate = album.releaseDate,
                description = album.description,
                genre = album.genre,
                recordLabel = album.recordLabel,
            )
        )
    }
}

/** Preferencias en memoria en lugar de DataStore. */
class FakePreferencesRepository : PreferencesRepository {

    override val role = MutableStateFlow<Role?>(null)
    override val themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    override suspend fun setRole(role: Role) {
        this.role.value = role
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }
}
