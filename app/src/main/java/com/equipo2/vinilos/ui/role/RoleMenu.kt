package com.equipo2.vinilos.ui.role

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.equipo2.vinilos.R
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.databinding.PopupRoleMenuBinding
import java.util.Locale

/**
 * Menú del chip de rol: rol actual, cambio de rol, modo oscuro y volver al inicio.
 * Es un PopupWindow con un layout propio porque PopupMenu no admite el interruptor de modo oscuro.
 */
class RoleMenu(
    private val anchor: View,
    private val onRoleSelected: (Role) -> Unit,
    private val onDarkModeChanged: (Boolean) -> Unit,
    private val onGoHome: () -> Unit,
) {

    private var popup: PopupWindow? = null

    val isShowing: Boolean
        get() = popup?.isShowing == true

    fun show(role: Role?, isDark: Boolean, animate: Boolean = true) {
        val context = anchor.context
        val resources = context.resources
        val binding = PopupRoleMenuBinding.inflate(LayoutInflater.from(context))
        val width = resources.getDimensionPixelSize(R.dimen.role_menu_width)
        val popup = PopupWindow(binding.root, width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_popup_menu))
        popup.elevation = resources.getDimension(R.dimen.popup_elevation)
        if (!animate) popup.animationStyle = 0

        val roleName = context.getString(roleNameRes(role))
        binding.menuHeader.text = context.getString(R.string.role_menu_current, roleName.uppercase(Locale.forLanguageTag("es")))
        binding.checkVisitor.isInvisible = role != Role.VISITOR
        binding.checkCollector.isInvisible = role != Role.COLLECTOR
        binding.switchDarkMode.isChecked = isDark

        binding.optionVisitor.setOnClickListener { selectRole(Role.VISITOR) }
        binding.optionCollector.setOnClickListener { selectRole(Role.COLLECTOR) }
        binding.optionDarkMode.setOnClickListener { binding.switchDarkMode.toggle() }
        binding.switchDarkMode.setOnCheckedChangeListener { switch, checked ->
            switch.isEnabled = false
            onDarkModeChanged(checked)
        }
        binding.optionGoHome.setOnClickListener {
            dismiss()
            onGoHome()
        }

        this.popup = popup
        popup.showAsDropDown(anchor, 0, resources.getDimensionPixelOffset(R.dimen.role_menu_offset), Gravity.END)
        dimBehind(popup)
    }

    fun dismiss() {
        popup?.dismiss()
        popup = null
    }

    /** Oscurece la pantalla detrás del menú, como en un diálogo. */
    private fun dimBehind(popup: PopupWindow) {
        val window = popup.contentView.rootView
        val params = window.layoutParams as WindowManager.LayoutParams
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        params.dimAmount = DIM_AMOUNT
        (anchor.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).updateViewLayout(window, params)
    }

    private fun selectRole(role: Role) {
        dismiss()
        onRoleSelected(role)
    }

    companion object {
        /** Opacidad del velo de Material 3 para elementos modales. */
        private const val DIM_AMOUNT = 0.32f

        fun roleNameRes(role: Role?): Int = if (role == Role.COLLECTOR) R.string.role_collector else R.string.role_visitor
    }
}
