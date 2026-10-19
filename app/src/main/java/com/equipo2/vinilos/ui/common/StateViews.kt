package com.equipo2.vinilos.ui.common

import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.equipo2.vinilos.R
import com.equipo2.vinilos.databinding.ViewLoadingBinding
import com.equipo2.vinilos.databinding.ViewStateMessageBinding

/*
 * Componentes compartidos de estado. Una pantalla que carga datos los incluye en su layout:
 *
 *   <include android:id="@+id/loading" layout="@layout/view_loading" android:visibility="gone" />
 *   <include android:id="@+id/message" layout="@layout/view_state_message" android:visibility="gone" />
 *
 * y los pinta según su UiState:
 *
 *   binding.loading.root.isVisible = state is UiState.Loading
 *   binding.message.root.isVisible = state is UiState.Error || state is UiState.Empty
 *   when (state) {
 *       UiState.Loading -> binding.loading.showLoading(getString(R.string.loading_albums))
 *       UiState.Error -> binding.message.showError(getString(R.string.error_obj_albums)) { viewModel.load() }
 *       UiState.Empty -> binding.message.showEmpty(R.drawable.ic_album, title, body)
 *       is UiState.Success -> …
 *   }
 */

/** LoadingState: barra lineal, texto y bloques de carga. */
fun ViewLoadingBinding.showLoading(text: CharSequence) {
    loadingText.text = text
}

/** EmptyState: círculo con ícono, título y texto. */
fun ViewStateMessageBinding.showEmpty(@DrawableRes icon: Int, title: CharSequence, body: CharSequence) {
    stateIcon.setImageResource(icon)
    stateTitle.text = title
    stateBody.text = body
    stateRetry.isVisible = false
}

/** ErrorState: «No pudimos cargar {objeto}» con el botón «Reintentar». */
fun ViewStateMessageBinding.showError(objectName: CharSequence, onRetry: () -> Unit) {
    val context = root.context
    stateIcon.setImageResource(R.drawable.ic_cloud_off)
    stateTitle.text = context.getString(R.string.error_load_title, objectName)
    stateBody.text = context.getString(R.string.error_network_body)
    stateRetry.isVisible = true
    stateRetry.setOnClickListener { onRetry() }
}
