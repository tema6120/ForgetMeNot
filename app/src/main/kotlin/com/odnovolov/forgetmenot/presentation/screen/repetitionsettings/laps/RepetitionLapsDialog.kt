package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsEvent.*
import kotlinx.android.synthetic.main.dialog_repetition_laps.view.*
import kotlinx.coroutines.launch

class RepetitionLapsDialog : BaseDialogFragment() {
    init {
        RepetitionSettingsDiScope.reopenIfClosed()
        RepetitionLapsDiScope.reopenIfClosed()
    }

    private var controller: RepetitionLapsController? = null
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onCreateDialog()
        rootView = View.inflate(context, R.layout.dialog_repetition_laps, null)
        setupView()
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_number_of_laps)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(OkButtonClicked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun setupView() {
        with(rootView) {
            lapsButton.setOnClickListener { controller?.dispatch(LapsRadioButtonClicked) }
            infinitelyButton.setOnClickListener {
                controller?.dispatch(InfinitelyRadioButtonClicked)
            }
            lapsEditText.observeText { text: String ->
                controller?.dispatch(LapsInputChanged(text))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewCoroutineScope!!.launch {
            val diScope = RepetitionLapsDiScope.get()
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun observeViewModel(viewModel: RepetitionLapsViewModel) {
        with(rootView) {
            with(viewModel) {
                lapsEditText.setText(numberOfLapsInput)
                isInfinitely.observe { isInfinitely: Boolean ->
                    if (isInfinitely) {
                        infinitelyRadioButton.isChecked = true
                        lapsRadioButton.isChecked = false
                        lapsRadioButton.jumpDrawablesToCurrentState()
                        lapsEditText.isEnabled = false
                        lapsEditText.isClickable = false
                    } else {
                        infinitelyRadioButton.isChecked = false
                        infinitelyRadioButton.jumpDrawablesToCurrentState()
                        lapsRadioButton.isChecked = true
                        lapsEditText.isEnabled = true
                        lapsEditText.selectAll()
                        lapsEditText.showSoftInput(showImplicit = true)
                    }
                    if (infinitelyRadioButton.visibility == View.INVISIBLE) {
                        infinitelyRadioButton.jumpDrawablesToCurrentState()
                        infinitelyRadioButton.visibility = View.VISIBLE
                    }
                    if (lapsRadioButton.visibility == View.INVISIBLE) {
                        lapsRadioButton.jumpDrawablesToCurrentState()
                        lapsRadioButton.visibility = View.VISIBLE
                    }
                }
                numberOfLaps.observe { numberOfLaps: Int ->
                    lapsRadioButton.text = resources
                        .getQuantityText(R.plurals.number_of_laps, numberOfLaps)
                }
                isOkButtonEnabled.observe { isEnabled ->
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        isEnabled
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            RepetitionLapsDiScope.close()
        }
    }
}