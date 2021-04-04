package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.slider.Slider
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination.ForDarkTheme
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination.ForLightTheme
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog.CardTexOpacityDialogEvent.OkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog.CardTexOpacityDialogEvent.TextOpacityWasSelected
import kotlinx.android.synthetic.main.dialog_card_text_opacity.view.*
import kotlinx.coroutines.launch

class CardTexOpacityDialog : BaseDialogFragment() {
    init {
        CardAppearanceDiScope.reopenIfClosed()
    }

    private var controller: CardTexOpacityController? = null
    private lateinit var contentView: View
    private lateinit var destination: TextOpacityDialogDestination

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        contentView = View.inflate(requireContext(), R.layout.dialog_card_text_opacity, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardAppearanceDiScope.getAsync() ?: return@launch
            controller = diScope.cardTexOpacityController
            observeViewModel(diScope.cardTexOpacityViewModel)
        }
        return AlertDialog.Builder(requireContext())
            .setView(contentView)
            .create()
            .apply {
                window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(context, R.drawable.background_dialog_text_opacity)
                )
            }
    }

    private fun setupView() {
        with(contentView) {
            textOpacitySlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    controller?.dispatch(TextOpacityWasSelected(slider.value))
                }
            })
            cancelButton.setOnClickListener {
                dismiss()
            }
            okButton.setOnClickListener {
                controller?.dispatch(OkButtonClicked)
                dismiss()
            }
        }
    }

    private fun observeViewModel(viewModel: CardTexOpacityViewModel) {
        with(contentView) {
            destination = viewModel.destination ?: run {
                dismiss()
                return
            }
            textOpacitySlider.addOnChangeListener { _, value: Float, _ ->
                setExampleText(value)
                setExampleTextColor(value)
            }
            textOpacityExampleTextView.background = when (destination) {
                ForLightTheme -> {
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.example_frame_text_opacity_in_light_theme
                    )
                }
                ForDarkTheme -> {
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.example_frame_text_opacity_in_dark_theme
                    )
                }
            }
            textOpacitySlider.value = viewModel.textOpacity
        }
    }

    private fun setExampleText(sliderValue: Float) {
        val percent = "${(sliderValue * 100).toInt()} %"
        contentView.textOpacityExampleTextView.text =
            getString(R.string.text_opacity_example_text, percent)
    }

    private fun setExampleTextColor(sliderValue: Float) {
        val sourceColor: Int = when (destination) {
            ForLightTheme -> Color.BLACK
            ForDarkTheme -> Color.WHITE
        }
        val alpha: Int = (sliderValue * 0xFF).toInt()
        val exampleTextColor: Int = ColorUtils.setAlphaComponent(sourceColor, alpha)
        contentView.textOpacityExampleTextView.setTextColor(exampleTextColor)
    }
}