package com.equipo2.vinilos.ui.albums

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.equipo2.vinilos.MainActivity
import com.equipo2.vinilos.R
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.databinding.FragmentAlbumListBinding
import com.equipo2.vinilos.ui.common.Motion
import com.equipo2.vinilos.ui.common.useTabTransitions
import com.equipo2.vinilos.ui.session.SessionViewModel
import kotlinx.coroutines.launch

/** Pestaña Álbumes. El catálogo llega con HU01; HU07 agrega el botón + que solo ve el coleccionista. */
class AlbumListFragment : Fragment(R.layout.fragment_album_list) {

    private val session: SessionViewModel by activityViewModels { SessionViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useTabTransitions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAlbumListBinding.bind(view)
        binding.fabCreate.setOnClickListener { openCreateAlbum() }
        showCreatedAlbumNotice(binding)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                session.role.collect { role -> binding.fabCreate.isVisible = role == Role.COLLECTOR }
            }
        }
    }

    /** Al volver de Crear álbum con un álbum guardado, avisa «Álbum creado». */
    private fun showCreatedAlbumNotice(binding: FragmentAlbumListBinding) {
        val result = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        result.getLiveData<Int>(KEY_CREATED_ALBUM_ID).observe(viewLifecycleOwner) {
            result.remove<Int>(KEY_CREATED_ALBUM_ID)
            (requireActivity() as MainActivity).showSnackbar(getString(R.string.snackbar_album_created), anchor = binding.fabCreate)
        }
    }

    private fun openCreateAlbum() {
        // Crear álbum es una pantalla completa: esta pantalla sale y vuelve por el eje Y en lugar de con fade through.
        exitTransition = Motion.openFullScreen()
        reenterTransition = Motion.closeFullScreen()
        findNavController().navigate(R.id.action_albums_to_create)
    }

    companion object {
        /** Resultado que deja Crear álbum: el id del álbum guardado. HU01 lo usará para marcarlo como «Nuevo». */
        const val KEY_CREATED_ALBUM_ID = "createdAlbumId"
    }
}
