package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import kotlinx.android.synthetic.main.article_motivational_timer.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.*
import kotlinx.android.synthetic.main.question.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MotivationalTimerHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    private class State : FlowMaker<State>() {
        var timeLeft: Int by flowMaker(DEFAULT_TIME_FOR_ANSWER)
        var isExpired: Boolean by flowMaker(false)
    }

    private val state = State()
    private var timerJob: Job? = null
    override val layoutRes: Int get() = R.layout.article_motivational_timer
    override val helpArticle: HelpArticle get() = HelpArticle.MotivationalTimer

    override fun setupView() {
        questionTextView.setText(R.string.question_in_motivational_timer_article)
        answerTextView.setText(R.string.answer_in_motivational_timer_article)
        showAnswerButton.setOnClickListener {
            stopTimer()
            showAnswer()
        }
        timerButton.setOnClickListener {
            stopTimer()
        }
        observeState()
        if (state.timeLeft > 0) {
            startTimer()
        }
    }

    private fun showAnswer() {
        showAnswerButton.isVisible = false
        answerScrollView.isVisible = true
    }

    private fun observeState() {
        state.flowOf(State::timeLeft).observe { timeLeft: Int ->
            timerButton.setImageResource(
                when {
                    timeLeft == 0 -> R.drawable.ic_timer_white_24dp_off
                    timeLeft % 2 == 1 -> R.drawable.ic_timer_white_24dp_odd
                    else -> R.drawable.ic_timer_white_24dp
                }
            )
            if (timeLeft == 0) {
                timerButton.setOnClickListener {
                    showToast(R.string.toast_timer_is_already_off)
                }
            }
        }
        state.flowOf(State::isExpired).observe { isExpired: Boolean ->
            cardView.setCardBackgroundColor(
                if (isExpired) {
                    ContextCompat.getColor(requireContext(), R.color.background_expired_card)
                } else {
                    Color.WHITE
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (state.timeLeft > 0 && viewCoroutineScope != null) {
            startTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (state.timeLeft > 0) {
            resetTimer()
        }
    }

    private fun startTimer() {
        timerJob = viewCoroutineScope!!.launch {
            while (state.timeLeft > 0) {
                delay(1000)
                state.timeLeft--
            }
            if (isActive) {
                state.isExpired = true
                showAnswer()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        state.timeLeft = 0
    }

    private fun resetTimer() {
        timerJob?.cancel()
        state.timeLeft = DEFAULT_TIME_FOR_ANSWER
    }

    private companion object {
        const val DEFAULT_TIME_FOR_ANSWER = 15
    }
}