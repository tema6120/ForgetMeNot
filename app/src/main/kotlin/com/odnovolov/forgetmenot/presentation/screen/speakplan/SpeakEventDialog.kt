package com.odnovolov.forgetmenot.presentation.screen.speakplan

import SPEAK_PLAN_SCOPE_ID
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import kotlinx.android.synthetic.main.dialog_speak_event.view.*
import org.koin.android.ext.android.getKoin

class SpeakEventDialog : BaseDialogFragment() {
    private val koinScope = getKoin().getScope(SPEAK_PLAN_SCOPE_ID)
    private val viewModel: SpeakPlanViewModel by koinScope.inject()
    private val controller: SpeakPlanController by koinScope.inject()
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_speak_event, null)
        setupView()
        observeViewModel(isRestoring = savedInstanceState != null)
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_dialog_speak_event)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ -> controller.onDialogOkButtonClicked() }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun setupView() {
        with(rootView) {
            speakQuestionButton.setOnClickListener {
                controller.onSpeakQuestionRadioButtonClicked()
                dismiss()
            }
            speakAnswerButton.setOnClickListener {
                controller.onSpeakAnswerRadioButtonClicked()
                dismiss()
            }
            delayButton.setOnClickListener {
                controller.onDelayButtonClicked()
            }
            delayEditText.observeText { controller.onDelayInputChanged(it.toString()) }
        }
    }

    private fun observeViewModel(isRestoring: Boolean) {
        with(viewModel) {
            with(rootView) {
                if (!isRestoring) {
                    delayEditText.setText(delayText)
                }
                selectedSpeakEvent.observe { speakEvent: SpeakEventDialogState.SpeakEvent? ->
                    speakQuestionRadioButton.isChecked =
                        speakEvent == SpeakEventDialogState.SpeakEvent.SpeakQuestion
                    speakAnswerRadioButton.isChecked =
                        speakEvent == SpeakEventDialogState.SpeakEvent.SpeakAnswer
                    delayRadioButton.isChecked =
                        speakEvent == SpeakEventDialogState.SpeakEvent.Delay
                    delayEditText.isEnabled = delayRadioButton.isChecked
                    if (speakQuestionRadioButton.visibility == INVISIBLE) {
                        speakQuestionRadioButton.jumpDrawablesToCurrentState()
                        speakQuestionRadioButton.visibility = VISIBLE
                        speakAnswerRadioButton.jumpDrawablesToCurrentState()
                        speakAnswerRadioButton.visibility = VISIBLE
                        delayRadioButton.jumpDrawablesToCurrentState()
                        delayRadioButton.visibility = VISIBLE
                    }
                    if (delayRadioButton.isChecked && isPortraitOrientation()) {
                        delayEditText.selectAll()
                        delayEditText.showSoftInput()
                    }
                }
                isOkButtonEnabled.observe { isEnabled: Boolean ->
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        isEnabled
                }
            }
        }
    }

    private fun isPortraitOrientation() =
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}