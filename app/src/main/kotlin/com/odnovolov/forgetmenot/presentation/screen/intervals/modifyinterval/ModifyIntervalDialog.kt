package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalEvent.*
import kotlinx.android.synthetic.main.dialog_last_tested_filter.view.*
import kotlinx.android.synthetic.main.dialog_modify_interval.view.*
import kotlinx.android.synthetic.main.dialog_modify_interval.view.bottomDivider
import kotlinx.android.synthetic.main.dialog_modify_interval.view.cancelButton
import kotlinx.android.synthetic.main.dialog_modify_interval.view.okButton
import kotlinx.android.synthetic.main.dialog_modify_interval.view.unitPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModifyIntervalDialog : BaseDialogFragment() {
    init {
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
            val diScope = ModifyIntervalDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel, isRestoring)
        }
        return createDialog(rootView).apply {
            setOnShowListener { rootView.numberEditText.showSoftInput() }
        }
    }

    private fun setupView() {
        setupUnitPicker()
        setupNumberEditText()
        setupBottomButtons()
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

    private fun setupNumberEditText() {
        rootView.numberEditText.observeText { text: String ->
            controller?.dispatch(IntervalValueChanged(text))
            text.toIntOrNull()?.let(::updateIntervalUnitText)
        }
    }

    private fun updateIntervalUnitText(quantity: Int) {
        val intervalUnits = DisplayedInterval.IntervalUnit.values()
            .map { intervalUnit: DisplayedInterval.IntervalUnit ->
                val pluralsId: Int = when (intervalUnit) {
                    DisplayedInterval.IntervalUnit.Minutes -> R.plurals.interval_unit_minutes
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
        rootView.cancelButton.setOnClickListener {
            dismiss()
        }
        rootView.okButton.setOnClickListener {
            controller?.dispatch(OkButtonClicked)
            dismiss()
        }
    }

    private fun observeViewModel(viewModel: ModifyIntervalViewModel, isRestoring: Boolean) {
        if (!isRestoring) {
            rootView.numberEditText.run {
                setText(viewModel.intervalValueText)
                selectAll()
            }
        }
        setupGradeTextView(rootView.startGradeTextView, viewModel.grade)
        setupGradeTextView(rootView.endGradeTextView, viewModel.grade + 1)
        rootView.unitPicker.value = DisplayedInterval.IntervalUnit.values()
            .indexOf(viewModel.displayedIntervalUnit)
        viewModel.isOkButtonEnabled.observe(rootView.okButton::setEnabled)
    }


    private fun setupGradeTextView(gradeTextView: TextView, grade: Int) {
        val context = gradeTextView.context
        val gradeColorRes = getGradeColorRes(grade)
        val gradeColor: ColorStateList? = ContextCompat.getColorStateList(context, gradeColorRes)
        gradeTextView.backgroundTintList = gradeColor
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val shadowColorRes = getBrightGradeColorRes(grade)
            val brightGradeColor: Int = ContextCompat.getColor(context, shadowColorRes)
            gradeTextView.outlineAmbientShadowColor = brightGradeColor
            gradeTextView.outlineSpotShadowColor = brightGradeColor
        }
        gradeTextView.text = grade.toString()
    }

    override fun onResume() {
        super.onResume()
        rootView.numberEditText.showSoftInput()
        rootView.intervalDialogScrollView.viewTreeObserver
            .addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        rootView.intervalDialogScrollView.viewTreeObserver
            .removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollDown = rootView.intervalDialogScrollView.canScrollVertically(1)
        if (rootView.bottomDivider.isVisible != canScrollDown) {
            rootView.bottomDivider.isVisible = canScrollDown
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            ModifyIntervalDiScope.close()
        }
    }
}