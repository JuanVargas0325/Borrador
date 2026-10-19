package com.equipo2.vinilos.ui.albums.create

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.equipo2.vinilos.R
import com.equipo2.vinilos.ui.common.Motion

/** Pantalla completa para crear un álbum (HU07). */
class AlbumCreateFragment : Fragment(R.layout.fragment_album_create) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Motion.openFullScreen()
        returnTransition = Motion.closeFullScreen()
    }
}
