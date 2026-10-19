package com.equipo2.vinilos

import android.app.Application
import com.equipo2.vinilos.ui.common.ThemeModes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class VinilosApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        // El tema guardado se aplica antes de crear la primera pantalla para que no aparezca con el tema equivocado.
        val themeMode = runBlocking { ServiceLocator.preferencesRepository.themeMode.first() }
        ThemeModes.apply(themeMode)
    }
}
