package com.equipo2.vinilos.data.repository

import com.equipo2.vinilos.data.model.Album
import com.equipo2.vinilos.data.model.NewAlbum
import com.equipo2.vinilos.data.network.NetworkServiceAdapter

interface AlbumRepository {
    suspend fun createAlbum(album: NewAlbum): Result<Album>
}

class AlbumRepositoryImpl(private val adapter: NetworkServiceAdapter) : AlbumRepository {

    override suspend fun createAlbum(album: NewAlbum): Result<Album> = resultOf { adapter.createAlbum(album) }
}
