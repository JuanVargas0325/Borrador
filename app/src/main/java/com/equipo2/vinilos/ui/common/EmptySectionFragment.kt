package com.equipo2.vinilos.ui.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.equipo2.vinilos.R

/** Pestaña que todavía no tiene contenido; cada historia la reemplaza por su propio fragment. */
class EmptySectionFragment : Fragment(R.layout.fragment_empty_section) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useTabTransitions()
    }
}
