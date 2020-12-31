package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
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
            val diScope = ModifyIntervalDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel, isRestoring)
        }
        return AlertDialog.Builder(requireContext())
            .setView(rootView)
            .create()
            .apply {
                setOnShowListener { rootView.numberEditText.showSoftInput() }
                val window = window ?: return@apply
                window.setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_dialog)
                )
            }
    }

    private fun setupView() {
        setupNumberEditText()
        setupUnitPicker()
        setupBottomButtons()
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
        if (needToCloseDiScope()) {
            ModifyIntervalDiScope.close()
        }
    }
}