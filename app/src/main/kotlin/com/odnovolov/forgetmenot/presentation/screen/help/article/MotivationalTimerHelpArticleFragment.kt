package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.showToast
import kotlinx.android.synthetic.main.article_motivational_timer.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.*
import kotlinx.android.synthetic.main.question.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MotivationalTimerHelpArticleFragment : BaseFragment() {
    private class State : FlowableState<State>() {
        var timeLeft: Int by me(DEFAULT_TIME_FOR_ANSWER)
        var isExpired: Boolean by me(false)
    }

    private val state = State()
    private var timerJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_motivational_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeState()
    }

    private fun setupView() {
        questionTextView.setText(R.string.question_in_motivational_timer_article)
        answerTextView.setText(R.string.answer_in_motivational_timer_article)
        showAnswerButton.setOnClickListener {
            stopTimer()
            showAnswer()
        }
        timerButton.setOnClickListener {
            stopTimer()
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
        if (state.timeLeft > 0) {
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