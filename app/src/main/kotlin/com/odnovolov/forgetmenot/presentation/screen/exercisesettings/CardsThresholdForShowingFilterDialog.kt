package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.CardsThresholdForFilterDialogInputTextChanged
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.CardsThresholdForShowingFilterDialogOkButtonClicked
import kotlinx.android.synthetic.main.dialog_cards_threshold_for_showing_filter.view.*
import kotlinx.coroutines.launch

class CardsThresholdForShowingFilterDialog : BaseDialogFragment() {
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
                controller?.dispatch(CardsThresholdForFilterDialogInputTextChanged(text))
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            okButton.setOnClickListener {
                controller?.dispatch(CardsThresholdForShowingFilterDialogOkButtonClicked)
                dismiss()
            }
        }
    }

    private fun observeViewModel(
        viewModel: ExerciseSettingsViewModel,
        isDialogFirstCreated: Boolean
    ) {
        with(contentView) {
            if (isDialogFirstCreated) {
                dialogInput.setText(viewModel.cardsThresholdForFilterDialogInput)
                dialogInput.selectAll()
            }
            viewModel.isCardsThresholdForShowingFilterDialogOkButtonEnabled
                .observe(okButton::setEnabled)
        }
    }
}