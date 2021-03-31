package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardsThresholdDialogState.Purpose.ToChangeCardNumberThresholdForShowingFilter
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardsThresholdDialogState.Purpose.ToChangeCardNumberLimitation
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.CardsThresholdDialogInputTextChanged
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.CardsThresholdDialogOkButtonClicked
import kotlinx.android.synthetic.main.dialog_cards_threshold_for_showing_filter.view.*
import kotlinx.coroutines.launch

class CardThresholdDialog : BaseDialogFragment() {
    init {
        ExerciseSettingsDiScope.reopenIfClosed()
    }

    private var controller: ExerciseSettingsController? = null
    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        contentView = View.inflate(
            requireContext(),
            R.layout.dialog_cards_threshold_for_showing_filter,
            null
        )
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseSettingsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel, isDialogFirstCreated = savedInstanceState == null)
        }
        return createDialog(contentView).apply {
            setOnShowListener { contentView.dialogInput.showSoftInput() }
        }
    }

    private fun setupView() {
        with(contentView) {
            dialogInput.observeText { text: String ->
                controller?.dispatch(CardsThresholdDialogInputTextChanged(text))
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            okButton.setOnClickListener {
                controller?.dispatch(CardsThresholdDialogOkButtonClicked)
                dismiss()
            }
        }
    }

    private fun observeViewModel(
        viewModel: ExerciseSettingsViewModel,
        isDialogFirstCreated: Boolean
    ) {
        with(contentView) {
            dialogDescriptionTextView.setText(
                when (viewModel.cardsThresholdDialogPurpose) {
                    ToChangeCardNumberLimitation -> {
                        R.string.description_cards_threshold_dialog_to_change_card_number_limitation
                    }
                    ToChangeCardNumberThresholdForShowingFilter -> {
                        R.string.description_cards_threshold_dialog_to_change_card_number_threshold_for_showing_filter
                    }
                    null -> {
                        dismiss()
                        return
                    }
                }
            )
            if (isDialogFirstCreated) {
                dialogInput.setText(viewModel.cardsThresholdDialogText)
                dialogInput.selectAll()
            }
            viewModel.isCardsThresholdDialogOkButtonEnabled.observe(okButton::setEnabled)
        }
    }
}