package com.equipo2.vinilos.ui.common

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.equipo2.vinilos.R
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

/** Aviso de la app: ícono de éxito o error, texto, acción opcional y botón de cerrar. */
object AppSnackbar {

    private const val DURATION_MS = 4000

    private var current: WeakReference<Snackbar>? = null

    fun show(
        root: View,
        message: CharSequence,
        isError: Boolean = false,
        anchor: View? = null,
        actionText: CharSequence? = null,
        action: (() -> Unit)? = null,
    ) {
        dismissCurrent()
        val snackbar = Snackbar.make(root, message, DURATION_MS)
        if (anchor != null && anchor.isShown) snackbar.anchorView = anchor
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
            snackbar.setActionTextColor(ContextCompat.getColor(root.context, R.color.md_inverse_primary))
        }
        snackbar.decorate(isError)
        current = WeakReference(snackbar)
        snackbar.show()
    }

    /** Un aviso nuevo reemplaza al anterior de inmediato, sin esperar su animación de salida. */
    private fun dismissCurrent() {
        current?.get()?.let { previous ->
            previous.view.visibility = View.INVISIBLE
            previous.dismiss()
        }
    }

    private fun Snackbar.decorate(isError: Boolean) {
        val context = view.context
        val contentColor = ContextCompat.getColor(context, R.color.md_inverse_on_surface)
        val text = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        text.maxLines = 4

        val iconSize = context.resources.getDimensionPixelSize(R.dimen.snackbar_icon)
        val icon = AppCompatResources.getDrawable(context, if (isError) R.drawable.ic_error else R.drawable.ic_check)!!.mutate()
        DrawableCompat.setTint(icon, contentColor)
        icon.setBounds(0, 0, iconSize, iconSize)
        text.setCompoundDrawablesRelative(icon, null, null, null)
        text.compoundDrawablePadding = context.resources.getDimensionPixelSize(R.dimen.snackbar_icon_padding)

        // Material no trae botón de cerrar: se agrega al final del contenido del aviso.
        val content = text.parent as ViewGroup
        val close = ImageButton(context).apply {
            setImageResource(R.drawable.ic_close)
            setColorFilter(contentColor)
            contentDescription = context.getString(R.string.cd_close_snackbar)
            val ripple = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, ripple, true)
            setBackgroundResource(ripple.resourceId)
            setOnClickListener { dismiss() }
        }
        val touchTarget = context.resources.getDimensionPixelSize(R.dimen.touch_target)
        content.addView(close, LinearLayout.LayoutParams(touchTarget, touchTarget))
    }
}
