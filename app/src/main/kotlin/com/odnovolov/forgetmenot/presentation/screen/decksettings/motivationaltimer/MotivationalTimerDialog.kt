package com.odnovolov.forgetmenot.presentation.screen.decksettings.motivationaltimer

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksettings.motivationaltimer.MotivationalTimerEvent.*
import kotlinx.android.synthetic.main.dialog_motivational_timer.view.*
import kotlinx.coroutines.launch

class MotivationalTimerDialog : BaseDialogFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        MotivationalTimerDiScope.reopenIfClosed()
    }

    private var controller: MotivationalTimerController? = null
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_motivational_timer, null)
        setupView()
        return AlertDialog.Builder(requireContext())
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(OkButtonClicked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun setupView() {
        with(rootView) {
            timeForAnswerButton.setOnClickListener {
                controller?.dispatch(TimeForAnswerSwitchToggled)
            }
            timeForAnswerEditText.observeText { text: String ->
                controller?.dispatch(TimeInputChanged(text))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewCoroutineScope!!.launch {
            val diScope = MotivationalTimerDiScope.getAsync()
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun observeViewModel(viewModel: MotivationalTimerViewModel) {
        with(viewModel) {
            with(rootView) {
                timeForAnswerEditText.setText(timeInput)
                isTimerEnabled.observe { isTimerEnabled: Boolean ->
                    timeForAnswerSwitch.run {
                        isChecked = isTimerEnabled
                        uncover()
                    }
                    secTextView.isEnabled = isTimerEnabled
                    timeForAnswerEditText.run {
                        isEnabled = isTimerEnabled
                        if (isTimerEnabled) {
                            selectAll()
                            showSoftInput()
                        } else {
                            setSelection(0)
                        }
                    }
                }
                isOkButtonEnabled.observe { isEnabled: Boolean ->
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        isEnabled
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            MotivationalTimerDiScope.close()
        }
    }
}