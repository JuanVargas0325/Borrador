package com.equipo2.vinilos

import androidx.annotation.VisibleForTesting
import com.equipo2.vinilos.data.network.NetworkServiceAdapter
import com.equipo2.vinilos.data.repository.AlbumRepository
import com.equipo2.vinilos.data.repository.AlbumRepositoryImpl

/** Inyección de dependencias manual: crea cada dependencia una sola vez y la comparte. */
object ServiceLocator {

    private val networkServiceAdapter by lazy { NetworkServiceAdapter(BuildConfig.API_BASE_URL) }
    private val defaultAlbumRepository by lazy { AlbumRepositoryImpl(networkServiceAdapter) }

    @VisibleForTesting
    var albumRepositoryOverride: AlbumRepository? = null

    val albumRepository: AlbumRepository
        get() = albumRepositoryOverride ?: defaultAlbumRepository
}
