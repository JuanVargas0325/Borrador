package com.equipo2.vinilos.ui.albums.create

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import com.equipo2.vinilos.R
import com.equipo2.vinilos.data.model.AlbumCatalog
import com.equipo2.vinilos.databinding.FragmentAlbumCreateBinding
import com.equipo2.vinilos.domain.AlbumField
import com.equipo2.vinilos.domain.AlbumFormValidator
import com.equipo2.vinilos.ui.common.DateFormats
import com.equipo2.vinilos.ui.common.Motion
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Pantalla completa para crear un álbum (HU07). */
class AlbumCreateFragment : Fragment(R.layout.fragment_album_create) {

    private val viewModel: AlbumCreateViewModel by viewModels { AlbumCreateViewModel.Factory }
    private var loadedCover: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Motion.openFullScreen()
        returnTransition = Motion.closeFullScreen()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAlbumCreateBinding.bind(view)
        loadedCover = null
        bindInputs(binding)
        binding.btnSave.setOnClickListener {
            hideKeyboard(view)
            viewModel.save()
        }
        binding.btnCancel.setOnClickListener { requestClose() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { requestClose() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { render(binding, it) } }
                launch { viewModel.events.collect { handle(binding, it) } }
            }
        }
    }

    private fun bindInputs(binding: FragmentAlbumCreateBinding) {
        binding.etName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        binding.etCover.doAfterTextChanged { viewModel.onCoverChange(it.toString()) }
        binding.etDescription.doAfterTextChanged { viewModel.onDescriptionChange(it.toString()) }
        binding.etDate.setOnClickListener { openDatePicker() }
        binding.tilDate.setEndIconOnClickListener { openDatePicker() }
        binding.acGenre.setSimpleItems(AlbumCatalog.genres.toTypedArray())
        binding.acGenre.setOnItemClickListener { _, _, position, _ -> viewModel.onGenreChange(AlbumCatalog.genres[position]) }
        binding.acLabel.setSimpleItems(AlbumCatalog.recordLabels.toTypedArray())
        binding.acLabel.setOnItemClickListener { _, _, position, _ -> viewModel.onRecordLabelChange(AlbumCatalog.recordLabels[position]) }
    }

    private fun render(binding: FragmentAlbumCreateBinding, state: AlbumCreateUiState) {
        val form = state.form
        fun TextInputLayout.showError(field: AlbumField) {
            error = state.errors[field]?.message(requireContext(), field)
        }
        binding.tilName.showError(AlbumField.NAME)
        binding.tilCover.showError(AlbumField.COVER)
        binding.tilDate.showError(AlbumField.RELEASE_DATE)
        binding.tilGenre.showError(AlbumField.GENRE)
        binding.tilLabel.showError(AlbumField.RECORD_LABEL)
        binding.tilDescription.showError(AlbumField.DESCRIPTION)
        binding.tilDate.helperText = getString(R.string.help_release_date).takeUnless { AlbumField.RELEASE_DATE in state.errors }

        // Los campos de texto conservan lo escrito; solo se pintan los valores que se eligen fuera del teclado.
        val dateText = form.releaseDate?.let(DateFormats::long).orEmpty()
        if (binding.etDate.text.toString() != dateText) binding.etDate.setText(dateText)
        form.genre?.takeIf { it != binding.acGenre.text.toString() }?.let { binding.acGenre.setText(it, false) }
        form.recordLabel?.takeIf { it != binding.acLabel.text.toString() }?.let { binding.acLabel.setText(it, false) }
        renderCoverPreview(binding, form.cover.trim())
    }

    private fun renderCoverPreview(binding: FragmentAlbumCreateBinding, cover: String) {
        val isValid = AlbumFormValidator.isValidUrl(cover)
        binding.coverPreview.isVisible = isValid
        if (isValid && cover != loadedCover) {
            loadedCover = cover
            binding.tvCoverUrl.text = cover
            binding.ivCover.load(cover)
        }
    }

    private fun handle(binding: FragmentAlbumCreateBinding, event: AlbumCreateEvent) {
        when (event) {
            is AlbumCreateEvent.ShowErrors -> focusField(binding, event.firstField)
        }
    }

    /** Lleva el scroll y, si es un campo de texto, el foco al primer campo con error. */
    private fun focusField(binding: FragmentAlbumCreateBinding, field: AlbumField) {
        val target = when (field) {
            AlbumField.NAME -> binding.tilName
            AlbumField.COVER -> binding.tilCover
            AlbumField.RELEASE_DATE -> binding.tilDate
            AlbumField.GENRE -> binding.tilGenre
            AlbumField.RECORD_LABEL -> binding.tilLabel
            AlbumField.DESCRIPTION -> binding.tilDescription
        }
        binding.formScroll.post { binding.formScroll.smoothScrollTo(0, target.top) }
        target.editText?.takeIf { it.isFocusable }?.requestFocus()
    }

    private fun openDatePicker() {
        if (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        val today = LocalDate.now()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.label_release_date)
            .setSelection(DateFormats.toUtcMillis(viewModel.state.value.form.releaseDate ?: today))
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setEnd(DateFormats.toUtcMillis(today))
                    .setValidator(DateValidatorPointBackward.before(DateFormats.toUtcMillis(today)))
                    .build()
            )
            .setPositiveButtonText(R.string.action_accept)
            .setNegativeButtonText(R.string.action_cancel)
            .build()
        picker.addOnPositiveButtonClickListener { viewModel.onReleaseDateChange(DateFormats.fromUtcMillis(it)) }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    /** Cerrar, Cancelar o Atrás: si hay algo escrito, pide confirmación antes de descartarlo. */
    private fun requestClose() {
        if (viewModel.state.value.form.isEmpty) {
            findNavController().navigateUp()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.discard_title)
            .setMessage(R.string.discard_body)
            .setNegativeButton(R.string.discard_keep, null)
            .setPositiveButton(R.string.discard_confirm) { _, _ -> findNavController().navigateUp() }
            .show()
    }

    private fun hideKeyboard(view: View) {
        val keyboard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private companion object {
        const val DATE_PICKER_TAG = "release_date_picker"
    }
}
