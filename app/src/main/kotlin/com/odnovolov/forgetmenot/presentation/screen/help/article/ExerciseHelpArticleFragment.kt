package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.color
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.Prompter
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseToDemonstrateCardsRetesting.*
import kotlinx.android.synthetic.main.article_exercise.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.*
import kotlinx.android.synthetic.main.popup_choose_hint.view.*
import kotlinx.android.synthetic.main.question.view.*

class ExerciseHelpArticleFragment : BaseFragment() {
    private class Example2State : FlowableState<Example2State>() {
        var isLearned by me(false)
    }

    private class Example3State : FlowableState<Example3State>() {
        var hint: String? by me(null)
    }

    private val example2State = Example2State()
    private val example3State = Example3State()
    private val exercise by lazy(::createExercise)
    private val chooseHintPopup: PopupWindow by lazy(::createChooseHintPopup)

    private fun createExercise(): ExampleExerciseToDemonstrateCardsRetesting {
        fun createExerciseCard(questionId: Int, answerId: Int): ExerciseCard {
            val card = Card(
                id = generateId(),
                question = getString(questionId),
                answer = getString(answerId),
                levelOfKnowledge = 3
            )
            return ExerciseCard(
                id = generateId(),
                card = card
            )
        }

        val exerciseCards: List<ExerciseCard> = listOf(
            createExerciseCard(
                R.string.question1_in_exercise_article,
                R.string.answer1_in_exercise_article
            ),
            createExerciseCard(
                R.string.question2_in_exercise_article,
                R.string.answer2_in_exercise_article
            ),
            createExerciseCard(
                R.string.question3_in_exercise_article,
                R.string.answer3_in_exercise_article
            )
        )
        return ExampleExerciseToDemonstrateCardsRetesting(
            state = ExampleExerciseToDemonstrateCardsRetesting.State(exerciseCards)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExampleOfRetestingCards()
        setupExampleOfLearnedCard()
        setupExampleOfHints()
    }

    private fun setupExampleOfRetestingCards() {
        val adapter = ExampleExerciseCardAdapter(viewCoroutineScope!!, exercise)
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = adapter
        exercise.state.flowOf(State::exerciseCards).observe(adapter::submitList)
    }

    private fun setupExampleOfLearnedCard() {
        with(learnedCardExample) {
            questionTextView.setText(R.string.question4_in_exercise_article)
            answerTextView.setText(R.string.answer4_in_exercise_article)
            showAnswerButton.isVisible = false
            answerScrollView.isVisible = true
            notAskButton.setOnClickListener {
                example2State.isLearned = !example2State.isLearned
            }
            example2State.flowOf(Example2State::isLearned).observe { isLearned: Boolean ->
                with(notAskButton) {
                    setImageResource(
                        if (isLearned)
                            R.drawable.ic_baseline_replay_white_24 else
                            R.drawable.ic_block_white_24dp
                    )
                    contentDescription = getString(
                        if (isLearned)
                            R.string.description_ask_again_button else
                            R.string.description_not_ask_button
                    )
                    TooltipCompat.setTooltipText(this, contentDescription)
                }
                questionTextView.isEnabled = !isLearned
                answerTextView.isEnabled = !isLearned
            }
        }
    }

    private fun setupExampleOfHints() {
        setupMaskedLettersHintExample()
        setupQuizHintExample()
    }

    private fun setupMaskedLettersHintExample() {
        with(maskedLettersHintExample) {
            questionTextView.setText(R.string.question5_in_exercise_article)
            answerTextView.setText(R.string.answer5_in_exercise_article)
            showAnswerButton.setOnClickListener {
                hintScrollView.isVisible = false
                hintDivider.isVisible = false
                showAnswerButton.isVisible = false
                answerScrollView.isVisible = true
                hintButton.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp_disabled)
                hintButton.setOnClickListener { showToast(R.string.toast_hint_is_not_accessible) }
            }
            hintButton.setOnClickListener {
                if (example3State.hint == null) {
                    showChooseHintPopup()
                } else {
                    val answer: String = getString(R.string.answer5_in_exercise_article)
                    example3State.hint = if (hintTextView.hasSelection()) {
                        val startIndex =
                            minOf(hintTextView.selectionStart, hintTextView.selectionEnd)
                        val endIndex = maxOf(hintTextView.selectionStart, hintTextView.selectionEnd)
                        Prompter.unmaskRange(answer, example3State.hint!!, startIndex, endIndex)
                    } else {
                        Prompter.unmaskFirst(answer, example3State.hint!!)
                    }
                }
            }
            TooltipCompat.setTooltipText(hintButton, hintButton.contentDescription)
            example3State.flowOf(Example3State::hint).observe(hintTextView::setText)
        }
    }

    private fun createChooseHintPopup(): PopupWindow {
        val content = View.inflate(requireContext(), R.layout.popup_choose_hint, null).apply {
            hintAsQuizButton.setOnClickListener {
                maskedLettersHintExample.isVisible = false
                quizHintExample.isVisible = true
                hintButton.isVisible = false
                chooseHintPopup.dismiss()
            }
            maskLettersButton.setOnClickListener {
                val answer: String = getString(R.string.answer5_in_exercise_article)
                val maskedAnswer: String = Prompter.maskLetters(answer)
                example3State.hint = maskedAnswer
                maskedLettersHintExample.hintScrollView.isVisible = true
                maskedLettersHintExample.hintDivider.isVisible = true
                chooseHintPopup.dismiss()
            }
        }
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        return PopupWindow(context).apply {
            width = content.measuredWidth
            height = content.measuredHeight
            contentView = content
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        color.exercise_control_panel_popup_background
                    )
                )
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
        }
    }

    private fun showChooseHintPopup() {
        val hintButtonLocation = IntArray(2).also { hintButton.getLocationOnScreen(it) }
        val x = hintButtonLocation[0] + 8.dp
        val y = hintButtonLocation[1] + hintButton.height - 8.dp - chooseHintPopup.height
        chooseHintPopup.showAtLocation(
            hintButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    private fun setupQuizHintExample() {
        with(quizHintExample) {
            questionTextView.setText(R.string.question5_in_exercise_article)
            variant1Button.setText(R.string.answer5_in_exercise_article)
            variant2Button.setText(R.string.variant2_in_exercise_article)
            variant3Button.setText(R.string.variant3_in_exercise_article)
            variant4Button.setText(R.string.variant4_in_exercise_article)

            variant1Button.background =
                ContextCompat.getDrawable(context, R.drawable.correct_answer_selector)

            variant1Button.setOnClickListener(::onVariantSelected)
            variant2Button.setOnClickListener(::onVariantSelected)
            variant3Button.setOnClickListener(::onVariantSelected)
            variant4Button.setOnClickListener(::onVariantSelected)
        }
    }

    private fun onVariantSelected(selectedButton: View) {
        variant1Button.isClickable = false
        variant2Button.isClickable = false
        variant3Button.isClickable = false
        variant4Button.isClickable = false
        variant1Button.isSelected = true

        if (selectedButton != variant1Button) {
            selectedButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.wrong_answer_selector)
            selectedButton.isSelected = true
        }
    }
}