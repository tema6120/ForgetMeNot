package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import kotlinx.android.synthetic.main.article_question_display.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.*
import kotlinx.android.synthetic.main.question.*
import java.util.*

class QuestionDisplayArticleFragment : BaseFragment() {
    private val speaker = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow,
        initialLanguage = QUESTION_LANGUAGE
    )
    private val speakErrorToast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.error_message_failed_to_speak, Toast.LENGTH_SHORT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_question_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        questionTextView.setText(R.string.question_in_question_display_article)
        answerTextView.setText(R.string.answer_in_question_display_article)
        showQuestionButton.run {
            isVisible = true
            setOnClickListener { isVisible = false }
        }
        showAnswerButton.setOnClickListener {
            showQuestionButton.isVisible = false
            showAnswerButton.isVisible = false
            answerScrollView.isVisible = true
        }
        speaker.state.flowOf(SpeakerImpl.State::isPreparingToSpeak).observe { isPreparingToSpeak ->
            speakProgressBar.isVisible = isPreparingToSpeak
        }
        speaker.state.flowOf(SpeakerImpl.State::isSpeaking).observe { isSpeaking ->
            with(speakButton) {
                setImageResource(
                    if (isSpeaking)
                        R.drawable.ic_volume_off_white_24dp else
                        R.drawable.ic_volume_up_white_24dp
                )
                setOnClickListener {
                    if (isSpeaking) speaker.stop() else onSpeakButtonClicked()
                }
                contentDescription = getString(
                    if (isSpeaking)
                        R.string.description_stop_speak_button else
                        R.string.description_speak_button
                )
                TooltipCompat.setTooltipText(this, contentDescription)
            }
        }
        speaker.events.observe { event: SpeakerImpl.Event ->
            when (event) {
                SpeakError -> speakErrorToast.show()
            }
        }
    }

    private fun onSpeakButtonClicked() {
        when {
            questionTextView.hasSelection() ->
                speaker.speak(questionTextView.selectedText, QUESTION_LANGUAGE)
            answerTextView.hasSelection() ->
                speaker.speak(answerTextView.selectedText, ANSWER_LANGUAGE)
            showAnswerButton.isVisible ->
                speaker.speak(questionTextView.text.toString(), QUESTION_LANGUAGE)
            else ->
                speaker.speak(answerTextView.text.toString(), ANSWER_LANGUAGE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speaker.shutdown()
    }

    private companion object {
        val QUESTION_LANGUAGE: Locale = Locale.FRENCH
        val ANSWER_LANGUAGE: Locale = Locale.ENGLISH
    }
}