package com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.lasttested

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.NumberPicker
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.lasttested.LastTestedFilterEvent.*
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import kotlinx.android.synthetic.main.dialog_last_tested_filter.view.*
import kotlinx.coroutines.launch

class LastTestedFilterDialog : BaseDialogFragment() {
    init {
        LastTestedFilterDiScope.reopenIfClosed()
    }

    private var controller: LastTestedFilterController? = null
    private lateinit var viewModel: LastTestedFilterViewModel
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_last_tested_filter, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = LastTestedFilterDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
        return createDialog(rootView)
    }

    private fun setupView() {
        setupRadioButtons()
        setupUnitPicker()
        setupValueEditText()
        setupBottomButtons()
    }

    private fun setupRadioButtons() {
        rootView.zeroTimeFrame.setOnClickListener {
            controller?.dispatch(ZeroTimeRadioButtonClicked)
        }
        rootView.specificTimeFrame.setOnClickListener {
            controller?.dispatch(SpecificTimeRadioButtonClicked)
        }
    }

    private fun setupUnitPicker() {
        with(rootView.unitPicker) {
            minValue = 0
            maxValue = DisplayedInterval.IntervalUnit.values().lastIndex
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            wrapSelectorWheel = false
            setOnValueChangedListener { _, _, newValue: Int ->
                val intervalUnit = DisplayedInterval.IntervalUnit.values()[newValue]
                controller?.dispatch(IntervalUnitChanged(intervalUnit))
            }
        }
    }

    private fun setupValueEditText() {
        rootView.valueEditText.observeText { text: String ->
            controller?.dispatch(IntervalValueChanged(text))
            text.toIntOrNull()?.let(::updateIntervalUnitText)
        }
    }

    private fun updateIntervalUnitText(quantity: Int) {
        val intervalUnits = DisplayedInterval.IntervalUnit.values()
            .map { intervalUnit: DisplayedInterval.IntervalUnit ->
                val pluralsId: Int = when (intervalUnit) {
                    DisplayedInterval.IntervalUnit.Minutes ->  R.plurals.interval_unit_minutes
                    DisplayedInterval.IntervalUnit.Hours -> R.plurals.interval_unit_hours
                    DisplayedInterval.IntervalUnit.Days -> R.plurals.interval_unit_days
                    DisplayedInterval.IntervalUnit.Months -> R.plurals.interval_unit_months
                }
                resources.getQuantityString(pluralsId, quantity)
            }
            .toTypedArray()
        rootView.unitPicker.displayedValues = intervalUnits
    }

    private fun setupBottomButtons() {
        rootView.okButton.setOnClickListener {
            controller?.dispatch(OkButtonClicked)
            dismiss()
        }
        rootView.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            with(rootView) {
                dialogTitle.setText(
                    if (isFromDialog)
                        R.string.title_dialog_last_tested_from else
                        R.string.title_dialog_last_tested_to
                )
                zeroTimeRadioButton.text = getString(
                    if (isFromDialog) R.string.zero_time else R.string.now
                )

                valueEditText.setText(intervalValueText)

                unitPicker.value =
                    DisplayedInterval.IntervalUnit.values().indexOf(displayedIntervalUnit)

                isZeroTimeSelected.observe { isZeroTimeSelected: Boolean ->
                    zeroTimeRadioButton.run {
                        isChecked = isZeroTimeSelected
                        uncover()
                    }
                    zeroTimeFrame.isClickable = !isZeroTimeSelected
                    specificTimeSpanRadioButton.run {
                        isChecked = !isZeroTimeSelected
                        uncover()
                    }
                    specificTimeFrame.isClickable = isZeroTimeSelected
                    setIsEnabledOfValueInputGroup(!isZeroTimeSelected)
                    valueEditText.run {
                        if (isZeroTimeSelected) {
                            setSelection(0)
                        } else {
                            selectAll()
                            showSoftInput()
                        }
                    }
                }

                isOkButtonEnabled.observe(okButton::setEnabled)
            }
        }
    }

    private fun setIsEnabledOfValueInputGroup(isEnabled: Boolean) {
        rootView.valueEditText.isEnabled = isEnabled
        rootView.unitPicker.isEnabled = isEnabled
        rootView.unitPicker.alpha = if (isEnabled) 1f else 0.3f
        rootView.agoTextView.isEnabled = isEnabled
    }

    override fun onResume() {
        super.onResume()
        rootView.scrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        rootView.scrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = rootView.scrollView.canScrollVertically(-1)
        if (rootView.topDivider.isVisible != canScrollUp) {
            rootView.topDivider.isVisible = canScrollUp
        }
        val canScrollDown = rootView.scrollView.canScrollVertically(1)
        if (rootView.bottomDivider.isVisible != canScrollDown) {
            rootView.bottomDivider.isVisible = canScrollDown
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            LastTestedFilterDiScope.close()
        }
    }
}