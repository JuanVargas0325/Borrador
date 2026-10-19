package com.equipo2.vinilos.ui.common

import androidx.fragment.app.Fragment
import androidx.transition.Fade
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis

/** Transiciones entre pantallas de la sección 6.2 de la guía, con Material motion. */
object Motion {

    private const val TAB_DURATION_MS = 300L
    private const val FADE_DURATION_MS = 300L
    private const val OPEN_FULL_SCREEN_MS = 400L
    private const val CLOSE_FULL_SCREEN_MS = 250L

    /** Cambio de pestaña: la pantalla que sale se desvanece y la que entra aparece con fade y escala 0,92 → 1. */
    fun fadeThrough() = MaterialFadeThrough().apply { duration = TAB_DURATION_MS }

    /** Entre la selección de rol y la app. */
    fun fade() = Fade().apply { duration = FADE_DURATION_MS }

    /** Abrir una pantalla completa: la nueva entra desde abajo y la anterior se va hacia arriba. */
    fun openFullScreen() = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply { duration = OPEN_FULL_SCREEN_MS }

    /** Cerrar una pantalla completa: el movimiento inverso, más rápido. */
    fun closeFullScreen() = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply { duration = CLOSE_FULL_SCREEN_MS }
}

fun Fragment.useTabTransitions() {
    enterTransition = Motion.fadeThrough()
    exitTransition = Motion.fadeThrough()
    reenterTransition = Motion.fadeThrough()
    returnTransition = Motion.fadeThrough()
}
