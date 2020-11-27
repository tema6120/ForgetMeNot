package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import kotlinx.android.synthetic.main.article_test_methods.*
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.answerScrollView as entryAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.answerScrollView as manualAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.answerTextView as manualAnswerTextView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.questionTextView as manualQuestionTextView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.answerScrollView as offAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.answerTextView as offAnswerTextView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.questionTextView as offQuestionTextView

class TestMethodsArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    override val layoutRes: Int get() = R.layout.article_test_methods
    override val helpArticle: HelpArticle get() = HelpArticle.TestMethods

    override fun setupView() {
        setupOffTestExerciseCard()
        setupManualTestExerciseCard()
        setupQuizTestExerciseCard()
        setupEntryTestExerciseCard()
    }

    private fun setupOffTestExerciseCard() {
        with(offTestExerciseCardExample) {
            offQuestionTextView.setText(R.string.question_example_off_test_method)
            offAnswerTextView.setText(R.string.answer_example_off_test_method)
            showAnswerButton.setOnClickListener {
                showAnswerButton.isVisible = false
                offAnswerScrollView.isVisible = true
            }
        }
    }

    private fun setupManualTestExerciseCard() {
        with(manualTestExerciseCardExample) {
            manualQuestionTextView.setText(R.string.question_example_manual_test_method)
            manualAnswerTextView.setText(R.string.answer_example_manual_test_method)
            rememberButton.setOnClickListener {
                manualAnswerScrollView.isVisible = true
                rememberButton.isSelected = true
                notRememberButton.isSelected = false
            }
            notRememberButton.setOnClickListener {
                manualAnswerScrollView.isVisible = true
                rememberButton.isSelected = false
                notRememberButton.isSelected = true
            }
        }
    }

    private fun setupQuizTestExerciseCard() {
        with(quizTestExerciseCardExample) {
            questionTextView.setText(R.string.question_example_quiz_test_method)
            variant1Button.setText(R.string.answer_example_quiz_test_method_variant1)
            variant2Button.setText(R.string.answer_example_quiz_test_method_variant2)
            variant3Button.setText(R.string.answer_example_quiz_test_method_variant3)
            variant4Button.setText(R.string.answer_example_quiz_test_method_variant4)

            variant4Button.background =
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
        variant4Button.isSelected = true

        if (selectedButton != variant4Button) {
            selectedButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.wrong_answer_selector)
            selectedButton.isSelected = true
        }
    }

    private fun setupEntryTestExerciseCard() {
        val correctAnswer: String = getString(R.string.answer_example_entry_test_method)
        with(entryTestExerciseCardExample) {
            questionTextView.setText(R.string.question_example_entry_test_method)
            correctAnswerTextView.text = correctAnswer
            checkButton.setOnClickListener {
                answerEditText.run {
                    isVisible = false
                    isEnabled = false
                    hideSoftInput()
                }
                checkButton.isVisible = false
                checkDivider.isVisible = false
                entryAnswerScrollView.isVisible = true
                val userAnswer: String? = answerEditText.text?.toString()?.trim()
                if (userAnswer != correctAnswer) {
                    wrongAnswerTextView.run {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = userAnswer
                        isVisible = true
                    }
                }
            }
        }
    }
}