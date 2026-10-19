package com.equipo2.vinilos

import com.equipo2.vinilos.data.model.Album
import com.equipo2.vinilos.data.model.NewAlbum
import com.equipo2.vinilos.data.repository.AlbumRepository
import java.io.IOException

/** Simula que el servidor falla al guardar: no se puede forzar un error en el backend compartido. */
class FailingAlbumRepository : AlbumRepository {
    override suspend fun createAlbum(album: NewAlbum): Result<Album> = Result.failure(IOException("Servidor no disponible"))
}
