package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import kotlinx.android.synthetic.main.dialog_modify_interval.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class ModifyIntervalFragment : DialogFragment() {
    private val coroutineScope = MainScope()
    private val koinScope =
        getKoin().getOrCreateScope<ModifyIntervalViewModel>(MODIFY_INTERVAL_SCOPE_ID)
    private val viewModel: ModifyIntervalViewModel by koinScope.viewModel(this)
    private val controller: ModifyIntervalController by koinScope.inject()
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView = View.inflate(requireContext(), R.layout.dialog_modify_interval, null)

        setupView()
        observeViewModel(isRestoring = savedInstanceState != null)

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_modify_interval)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ -> controller.onOkButtonClicked() }
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
            observeText { text -> controller.onIntervalValueChanged(text.toString()) }
        }
    }

    private fun setupUnitPicker() {
        val intervalUnits = DisplayedInterval.IntervalUnit.values()
            .map { it.name }
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

    private fun observeViewModel(isRestoring: Boolean) {
        if (!isRestoring) {
            rootView.numberEditText.run {
                setText(viewModel.intervalValueText)
                selectAll()
            }
        }
        rootView.unitPicker.value = DisplayedInterval.IntervalUnit.values()
            .indexOf(viewModel.displayedIntervalUnit)
        viewModel.isOkButtonEnabled.onEach { isEnabled ->
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isEnabled
        }
            .launchIn(coroutineScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        if (isRemoving) {
            controller.onFragmentRemoving()
        }
    }
}