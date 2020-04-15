package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterEvent.*
import kotlinx.android.synthetic.main.dialog_last_answer_filter.view.*
import kotlinx.coroutines.launch

class LastAnswerFilterDialog : BaseDialogFragment() {
    init {
        RepetitionSettingsDiScope.reopenIfClosed()
        LastAnswerFilterDiScope.reopenIfClosed()
    }

    private var controller: LastAnswerFilterController? = null
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_last_answer_filter, null)
        setupView()
        return AlertDialog.Builder(requireContext())
            .setTitle(" ") // need to set any title (but not empty) so that dialog will build with Title TextView
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(OkButtonClicked)
            }
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
            controller?.dispatch(ZeroTimeRadioButtonClicked)
        }
        rootView.specificTimeSpanButton.setOnClickListener {
            controller?.dispatch(SpecificTimeRadioButtonClicked)
        }
        rootView.specificTimeSpanRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                rootView.valueEditText.selectAll()
                rootView.valueEditText.showSoftInput(showImplicit = true)
            }
        }
    }

    private fun setupValueEditText() {
        rootView.valueEditText.observeText { text: String ->
            controller?.dispatch(IntervalValueChanged(text))
        }
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
                controller?.dispatch(IntervalUnitChanged(intervalUnit))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewCoroutineScope!!.launch {
            val diScope = LastAnswerFilterDiScope.get()
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun observeViewModel(viewModel: LastAnswerFilterViewModel) {
        with(viewModel) {
            val titleId: Int =
                if (isFromDialog)
                    R.string.last_answer_time_from_filter_dialog_title else
                    R.string.last_answer_time_to_filter_dialog_title
            dialog!!.setTitle(titleId)
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

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            LastAnswerFilterDiScope.close()
        }
    }
}