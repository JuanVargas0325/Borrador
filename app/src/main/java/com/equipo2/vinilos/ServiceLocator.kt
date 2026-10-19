package com.equipo2.vinilos

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.equipo2.vinilos.data.network.NetworkServiceAdapter
import com.equipo2.vinilos.data.repository.AlbumRepository
import com.equipo2.vinilos.data.repository.AlbumRepositoryImpl
import com.equipo2.vinilos.data.repository.DataStorePreferencesRepository
import com.equipo2.vinilos.data.repository.PreferencesRepository

/** Inyección de dependencias manual: crea cada dependencia una sola vez y la comparte. */
object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val networkServiceAdapter by lazy { NetworkServiceAdapter(BuildConfig.API_BASE_URL) }
    private val defaultAlbumRepository by lazy { AlbumRepositoryImpl(networkServiceAdapter) }

    @VisibleForTesting
    var albumRepositoryOverride: AlbumRepository? = null

    val albumRepository: AlbumRepository
        get() = albumRepositoryOverride ?: defaultAlbumRepository

    val preferencesRepository: PreferencesRepository by lazy { DataStorePreferencesRepository(appContext) }
}
