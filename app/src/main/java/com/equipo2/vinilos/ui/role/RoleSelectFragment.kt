package com.equipo2.vinilos.ui.role

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.equipo2.vinilos.R
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.databinding.FragmentRoleSelectBinding
import com.equipo2.vinilos.ui.common.Motion
import com.equipo2.vinilos.ui.common.ThemeModes
import com.equipo2.vinilos.ui.session.SessionViewModel

/** Primera pantalla: el usuario elige con qué rol entra. */
class RoleSelectFragment : Fragment(R.layout.fragment_role_select) {

    private val session: SessionViewModel by activityViewModels { SessionViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Motion.fade()
        exitTransition = Motion.fade()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentRoleSelectBinding.bind(view)
        binding.btnVisitor.setOnClickListener { enter(Role.VISITOR) }
        binding.btnCollector.setOnClickListener { enter(Role.COLLECTOR) }

        val isDark = ThemeModes.isDark(requireContext())
        binding.btnTheme.setImageResource(if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode)
        binding.btnTheme.contentDescription = getString(if (isDark) R.string.cd_dark_mode_off else R.string.cd_dark_mode_on)
        binding.btnTheme.setOnClickListener {
            session.setDarkMode(!isDark)
            ThemeModes.apply(session.themeMode.value)
        }
    }

    private fun enter(role: Role) {
        session.enterAs(role)
        findNavController().navigate(R.id.action_roleSelect_to_albums)
    }
}
