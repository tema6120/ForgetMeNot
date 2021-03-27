package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.TimerStatus
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import kotlinx.android.synthetic.main.article_motivational_timer.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.*
import kotlinx.android.synthetic.main.popup_timer.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    private var timerPopup: PopupWindow? = null
    private var timerButtonPaintingAnimation: ValueAnimator? = null
    private val timerStatus: Flow<TimerStatus> = combine(
            state.flowOf(State::timeLeft),
            state.flowOf(State::isExpired)
        ) { timeLeft: Int, isExpired: Boolean ->
            when {
                timeLeft > 0 -> TimerStatus.Ticking(timeLeft)
                isExpired -> TimerStatus.TimeIsOver
                else -> TimerStatus.Stopped
            }
        }

    override fun setupView() {
        questionTextView.setText(R.string.question_in_motivational_timer_article)
        questionScrollView.isVisible = true
        answerTextView.setText(R.string.answer_in_motivational_timer_article)
        showAnswerButton.setOnClickListener {
            stopTimer()
            showAnswer()
        }
        timerButton.setOnClickListener {
            requireTimerPopup().show(anchor = timerButton, Gravity.BOTTOM)
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

    private fun requireTimerPopup(): PopupWindow {
        if (timerPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_timer, null).apply {
                stopTimerButton.setOnClickListener {
                    stopTimer()
                    timerPopup?.dismiss()
                }
            }
            timerPopup = DarkPopupWindow(content)
            subscribeTimerPopupToViewModel()
        }
        return timerPopup!!
    }

    private fun subscribeTimerPopupToViewModel() {
        timerStatus.observe { timerStatus: TimerStatus ->
            timerPopup?.contentView?.run {
                timerIcon.setImageResource(
                    if (timerStatus is TimerStatus.Ticking && timerStatus.secondsLeft % 2 == 0)
                        R.drawable.ic_round_timer_24_even else
                        R.drawable.ic_round_timer_24
                )

                val iconColorRes: Int = when (timerStatus) {
                    is TimerStatus.Ticking -> R.color.ticking_timer_icon_on_popup
                    TimerStatus.TimeIsOver -> R.color.issue
                    else -> R.color.icon_on_control_panel_deactivated
                }
                timerIcon.setTintFromRes(iconColorRes)

                timerDescriptionTextView.text = when (timerStatus) {
                    TimerStatus.NotUsed -> null
                    TimerStatus.OffBecauseWalkingMode ->
                        getString(R.string.timer_is_off_because_walking_mode)
                    is TimerStatus.Ticking ->
                        getString(R.string.time_for_answer, timerStatus.secondsLeft)
                    TimerStatus.Stopped -> getString(R.string.timer_stopped)
                    TimerStatus.TimeIsOver -> getString(R.string.time_is_over)
                }

                val descriptionTextColorId: Int =
                    if (timerStatus == TimerStatus.TimeIsOver)
                        R.color.issue else
                        R.color.description_on_dark_popup
                timerDescriptionTextView.setTextColorFromRes(descriptionTextColorId)

                stopTimerButton.isVisible = timerStatus is TimerStatus.Ticking
            }
        }
    }

    private fun observeState() {
        timerStatus.observe(::onTimerStatusChanged)
        state.flowOf(State::isExpired).observe { isExpired: Boolean ->
            val cardBackgroundColorRes: Int =
                if (isExpired)
                    R.color.card_expired else
                    R.color.card
            val cardBackgroundColor: Int =
                ContextCompat.getColor(requireContext(), cardBackgroundColorRes)
            cardView.setCardBackgroundColor(cardBackgroundColor)
        }
    }

    private fun onTimerStatusChanged(timerStatus: TimerStatus) {
        if (timerStatus !is TimerStatus.Ticking ||
            timerStatus.secondsLeft * 1000L > ExerciseFragment.TIME_TO_PAINT_TIMER_BUTTON
        ) {
            timerButtonPaintingAnimation?.cancel()
            timerButtonPaintingAnimation = null
        }
        timerButton.setImageResource(
            if (timerStatus is TimerStatus.Ticking && timerStatus.secondsLeft % 2 == 0)
                R.drawable.ic_round_timer_24_even else
                R.drawable.ic_round_timer_24
        )
        if (timerStatus is TimerStatus.Ticking
            && timerStatus.secondsLeft * 1000L <= ExerciseFragment.TIME_TO_PAINT_TIMER_BUTTON
        ) {
            if (timerButtonPaintingAnimation == null && isResumed) {
                val colorFrom =
                    ContextCompat.getColor(requireContext(), R.color.icon_on_control_panel)
                val colorTo = ContextCompat.getColor(requireContext(), R.color.issue)
                timerButtonPaintingAnimation =
                    ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                        duration = timerStatus.secondsLeft * 1000L
                        addUpdateListener { animator: ValueAnimator ->
                            timerButton.setTint(animator.animatedValue as Int)
                        }
                        start()
                    }
            }
        } else {
            val iconColorRes: Int = when (timerStatus) {
                is TimerStatus.Ticking -> R.color.icon_on_control_panel
                TimerStatus.TimeIsOver -> R.color.issue
                else -> R.color.icon_on_control_panel_deactivated
            }
            timerButton.setTintFromRes(iconColorRes)
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
        timerButtonPaintingAnimation?.cancel()
        timerButtonPaintingAnimation = null
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