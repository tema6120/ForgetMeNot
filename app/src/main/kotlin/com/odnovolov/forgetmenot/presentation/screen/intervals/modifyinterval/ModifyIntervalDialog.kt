package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalEvent.*
import kotlinx.android.synthetic.main.dialog_modify_interval.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModifyIntervalDialog : BaseDialogFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        IntervalsDiScope.reopenIfClosed()
        ModifyIntervalDiScope.reopenIfClosed()
    }

    private var controller: ModifyIntervalController? = null
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_modify_interval, null)
        setupView()
        val isRestoring = savedInstanceState != null
        viewCoroutineScope!!.launch(Dispatchers.Main) {
            val diScope = ModifyIntervalDiScope.getAsync()
            controller = diScope.controller
            observeViewModel(diScope.viewModel, isRestoring)
        }
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_modify_interval)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(OkButtonClicked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply { setOnShowListener { rootView.numberEditText.showSoftInput() } }
    }

    private fun setupView() {
        setupNumberEditText()
        setupUnitPicker()
    }

    private fun setupNumberEditText() {
        rootView.numberEditText.observeText { text: String ->
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

    private fun observeViewModel(viewModel: ModifyIntervalViewModel, isRestoring: Boolean) {
        if (!isRestoring) {
            rootView.numberEditText.run {
                setText(viewModel.intervalValueText)
                selectAll()
            }
        }
        rootView.unitPicker.value = DisplayedInterval.IntervalUnit.values()
            .indexOf(viewModel.displayedIntervalUnit)
        viewModel.isOkButtonEnabled.observe { isEnabled ->
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isEnabled
        }
    }

    override fun onResume() {
        super.onResume()
        rootView.numberEditText.showSoftInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            ModifyIntervalDiScope.close()
        }
    }
}