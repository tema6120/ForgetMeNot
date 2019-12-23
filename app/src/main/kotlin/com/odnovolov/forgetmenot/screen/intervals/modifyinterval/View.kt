package com.odnovolov.forgetmenot.screen.intervals.modifyinterval

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.common.observeText
import com.odnovolov.forgetmenot.screen.intervals.IntervalUnit
import com.odnovolov.forgetmenot.screen.intervals.modifyinterval.ModifyIntervalEvent.*
import kotlinx.android.synthetic.main.dialog_modify_interval.view.*

class ModifyIntervalFragment : BaseDialogFragment() {

    private lateinit var rootView: View
    private val controller = ModifyIntervalController()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView = View.inflate(requireContext(), R.layout.dialog_modify_interval, null)

        setupView()
        observeViewModel(isRestoring = savedInstanceState != null)

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_modify_interval)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ -> controller.dispatch(OkButtonClicked) }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply { window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE) }
    }

    private fun setupView() {
        setupNumberEditText()
        setupUnitPicker()
    }

    private fun setupNumberEditText() {
        with(rootView.numberEditText) {
            requestFocus()
            observeText { text -> controller.dispatch(IntervalNumberChanged(text)) }
        }
    }

    private fun setupUnitPicker() {
        val intervalUnits = IntervalUnit.values()
            .map { it.name }
            .toTypedArray()
        with(rootView.unitPicker) {
            minValue = 0
            maxValue = intervalUnits.size - 1
            displayedValues = intervalUnits
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            wrapSelectorWheel = false
            setOnValueChangedListener { _, _, newValue: Int ->
                val intervalUnit: IntervalUnit = IntervalUnit.values()[newValue]
                controller.dispatch(IntervalUnitChanged(intervalUnit))
            }
        }
    }

    private fun observeViewModel(isRestoring: Boolean) {
        val viewModel = ModifyIntervalViewModel()
        if (!isRestoring) {
            rootView.numberEditText.run {
                setText(viewModel.intervalNumberText)
                selectAll()
            }
        }
        rootView.unitPicker.value = IntervalUnit.values().indexOf(viewModel.intervalUnit)
        viewModel.isOkButtonEnabled.observe { isEnabled ->
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isEnabled
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }

}