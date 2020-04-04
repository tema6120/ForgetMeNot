package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import LAST_ANSWER_FILTER_SCOPE_ID
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import kotlinx.android.synthetic.main.dialog_last_answer_filter.view.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class LastAnswerFilterDialog : BaseDialogFragment() {
    private val koinScope = getKoin()
        .getOrCreateScope<LastAnswerFilterViewModel>(LAST_ANSWER_FILTER_SCOPE_ID)
    private val viewModel: LastAnswerFilterViewModel by koinScope.viewModel(this)
    private val controller: LastAnswerFilterController by koinScope.inject()
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_last_answer_filter, null)
        setupView()
        observeViewModel()
        val titleId: Int =
            if (viewModel.isFromDialog) R.string.last_answer_time_from_filter_dialog_title
            else R.string.last_answer_time_to_filter_dialog_title
        return AlertDialog.Builder(requireContext())
            .setTitle(titleId)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ -> controller.onOkButtonClicked() }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun setupView() {
        setupRadioButtons()
        setupValueEditText()
        setupUnitPicker()
    }

    private fun setupRadioButtons() {
        rootView.zeroTimeButton.setOnClickListener {
            controller.onZeroTimeRadioButtonClicked()
        }
        rootView.specificTimeSpanButton.setOnClickListener {
            controller.onSpecificTimeRadioButtonClicked()
        }
        rootView.specificTimeSpanRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                rootView.valueEditText.selectAll()
                rootView.valueEditText.showSoftInput(showImplicit = true)
            }
        }
    }

    private fun setupValueEditText() {
        rootView.valueEditText.observeText(controller::onIntervalValueChanged)
    }

    private fun setupUnitPicker() {
        val intervalUnits = DisplayedInterval.IntervalUnit.values()
            .map { intervalUnit: DisplayedInterval.IntervalUnit ->
                getString(
                    when (intervalUnit) {
                        DisplayedInterval.IntervalUnit.Hours -> R.string.interval_unit_hours
                        DisplayedInterval.IntervalUnit.Days -> R.string.interval_unit_days
                        DisplayedInterval.IntervalUnit.Months -> R.string.interval_unit_months
                    }
                )
            }
            .toTypedArray()
        with(rootView.unitPicker) {
            minValue = 0
            maxValue = intervalUnits.size - 1
            displayedValues = intervalUnits
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            wrapSelectorWheel = false
            setOnValueChangedListener { _, _, newValue: Int ->
                val intervalUnit = DisplayedInterval.IntervalUnit.values()[newValue]
                controller.onIntervalUnitChanged(intervalUnit)
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            with(rootView) {
                zeroTimeRadioButton.text = getString(
                    if (isFromDialog) R.string.zero_time else R.string.now
                )

                valueEditText.setText(intervalValueText)

                unitPicker.value =
                    DisplayedInterval.IntervalUnit.values().indexOf(displayedIntervalUnit)

                isZeroTimeSelected.observe { isZeroTimeSelected: Boolean ->
                    if (isZeroTimeSelected) {
                        zeroTimeRadioButton.isChecked = true
                        specificTimeSpanRadioButton.isChecked = false
                        specificTimeSpanRadioButton.jumpDrawablesToCurrentState()
                        setIsEnabledOfValueInputGroup(false)
                    } else {
                        zeroTimeRadioButton.isChecked = false
                        zeroTimeRadioButton.jumpDrawablesToCurrentState()
                        specificTimeSpanRadioButton.isChecked = true
                        setIsEnabledOfValueInputGroup(true)
                    }
                    if (zeroTimeRadioButton.visibility == INVISIBLE) {
                        zeroTimeRadioButton.jumpDrawablesToCurrentState()
                        zeroTimeRadioButton.visibility = VISIBLE
                    }
                    if (specificTimeSpanRadioButton.visibility == INVISIBLE) {
                        specificTimeSpanRadioButton.jumpDrawablesToCurrentState()
                        specificTimeSpanRadioButton.visibility = VISIBLE
                    }
                }

                isOkButtonEnabled.observe { isEnabled ->
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        isEnabled
                }
            }
        }
    }

    private fun setIsEnabledOfValueInputGroup(isEnabled: Boolean) {
        rootView.valueEditText.isEnabled = isEnabled
        rootView.unitPicker.isEnabled = isEnabled
        rootView.agoTextView.isEnabled = isEnabled
    }

    override fun onPause() {
        super.onPause()
        if (!isRemoving) {
            controller.performSaving()
        }
    }
}