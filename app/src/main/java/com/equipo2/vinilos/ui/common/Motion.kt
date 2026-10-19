package com.equipo2.vinilos.ui.common

import androidx.fragment.app.Fragment
import androidx.transition.Fade
import com.google.android.material.transition.MaterialFadeThrough

/** Transiciones entre pantallas de la sección 6.2 de la guía, con Material motion. */
object Motion {

    private const val TAB_DURATION_MS = 300L
    private const val FADE_DURATION_MS = 300L

    /** Cambio de pestaña: la pantalla que sale se desvanece y la que entra aparece con fade y escala 0,92 → 1. */
    fun fadeThrough() = MaterialFadeThrough().apply { duration = TAB_DURATION_MS }

    /** Entre la selección de rol y la app. */
    fun fade() = Fade().apply { duration = FADE_DURATION_MS }
}

fun Fragment.useTabTransitions() {
    enterTransition = Motion.fadeThrough()
    exitTransition = Motion.fadeThrough()
    reenterTransition = Motion.fadeThrough()
    returnTransition = Motion.fadeThrough()
}
