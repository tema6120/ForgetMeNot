package com.odnovolov.forgetmenot.presentation.screen.speakplan

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanUiEvent.*
import kotlinx.android.synthetic.main.dialog_speak_event.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpeakEventDialog : BaseDialogFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        SpeakPlanDiScope.reopenIfClosed()
    }

    private var controller: SpeakPlanController? = null
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_speak_event, null)
        setupView()
        val isRestoring = savedInstanceState != null
        viewCoroutineScope!!.launch(Dispatchers.Main) {
            val diScope = SpeakPlanDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel, isRestoring)
        }
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_dialog_speak_event)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(DialogOkButtonClicked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun setupView() {
        with(rootView) {
            speakQuestionButton.setOnClickListener {
                controller?.dispatch(SpeakQuestionRadioButtonClicked)
                dismiss()
            }
            speakAnswerButton.setOnClickListener {
                controller?.dispatch(SpeakAnswerRadioButtonClicked)
                dismiss()
            }
            delayButton.setOnClickListener {
                controller?.dispatch(DelayButtonClicked)
            }
            delayEditText.observeText { text: String ->
                controller?.dispatch(DelayInputChanged(text))
            }
        }
    }

    private fun observeViewModel(viewModel: SpeakPlanViewModel, isRestoring: Boolean) {
        with(viewModel) {
            with(rootView) {
                if (!isRestoring) {
                    delayEditText.setText(delayText)
                }
                selectedSpeakEventType.observe { speakEventForm: SpeakEventType? ->
                    speakQuestionRadioButton.isChecked =
                        speakEventForm == SpeakEventType.SpeakQuestion
                    speakAnswerRadioButton.isChecked = speakEventForm == SpeakEventType.SpeakAnswer
                    delayRadioButton.isChecked = speakEventForm == SpeakEventType.Delay
                    delayEditText.isEnabled = delayRadioButton.isChecked
                    speakQuestionRadioButton.uncover()
                    speakAnswerRadioButton.uncover()
                    delayRadioButton.uncover()
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

    override fun onResume() {
        super.onResume()
        if (controller != null && rootView.delayRadioButton.isChecked && isPortraitOrientation()) {
            rootView.delayEditText.showSoftInput()
        }
    }

    private fun isPortraitOrientation() =
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}