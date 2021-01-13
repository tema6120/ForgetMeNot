package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import android.graphics.Paint
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.common.setFont
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import kotlinx.android.synthetic.main.article_testing_methods.*
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.answerScrollView as entryAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.questionTextView as entryQuestionTextView
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.questionScrollView as entryQuestionScrollView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.answerScrollView as manualAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.answerTextView as manualAnswerTextView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.questionScrollView as manualQuestionScrollView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.questionTextView as manualQuestionTextView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.answerScrollView as offAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.answerTextView as offAnswerTextView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.questionScrollView as offQuestionScrollView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.questionTextView as offQuestionTextView
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.questionFrame as quizQuestionFrame
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.questionScrollView as quizQuestionScrollView
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.questionTextView as quizQuestionTextView

class TestingMethodsArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    override val layoutRes: Int get() = R.layout.article_testing_methods
    override val helpArticle: HelpArticle get() = HelpArticle.TestingMethods

    override fun setupView() {
        paragraph1.setFont(R.font.nunito_bold)
        off_section_title.setFont(R.font.nunito_extrabold)
        off_section_description.setFont(R.font.nunito_bold)
        manual_section_title.setFont(R.font.nunito_extrabold)
        manual_section_description.setFont(R.font.nunito_bold)
        quiz_section_title.setFont(R.font.nunito_extrabold)
        quiz_section_description.setFont(R.font.nunito_bold)
        entry_section_title.setFont(R.font.nunito_extrabold)
        entry_section_description.setFont(R.font.nunito_bold)

        setupOffTestExerciseCard()
        setupManualTestExerciseCard()
        setupQuizTestExerciseCard()
        setupEntryTestExerciseCard()
    }

    private fun setupOffTestExerciseCard() {
        with(offTestExerciseCardExample) {
            offQuestionTextView.setText(R.string.question_example_off_testing_method)
            offQuestionScrollView.isVisible = true
            offAnswerTextView.setText(R.string.answer_example_off_testing_method)
            showAnswerButton.setOnClickListener {
                showAnswerButton.isVisible = false
                offAnswerScrollView.isVisible = true
            }
        }
    }

    private fun setupManualTestExerciseCard() {
        with(manualTestExerciseCardExample) {
            manualQuestionTextView.setText(R.string.question_example_manual_testing_method)
            manualQuestionScrollView.isVisible = true
            manualAnswerTextView.setText(R.string.answer_example_manual_testing_method)
            rememberButton.setOnClickListener {
                curtainView.isVisible = false
                manualAnswerScrollView.isVisible = true
                rememberButton.isSelected = true
                notRememberButton.isSelected = false
            }
            notRememberButton.setOnClickListener {
                curtainView.isVisible = false
                manualAnswerScrollView.isVisible = true
                rememberButton.isSelected = false
                notRememberButton.isSelected = true
            }
        }
    }

    private fun setupQuizTestExerciseCard() {
        with(quizTestExerciseCardExample) {
            quizQuestionFrame.updateLayoutParams<LinearLayout.LayoutParams> {
                height = 0
                weight = 1f
            }
            variantsScrollView.updateLayoutParams<LinearLayout.LayoutParams> {
                height = LayoutParams.WRAP_CONTENT
                weight = 0f
            }
            quizQuestionTextView.setText(R.string.question_example_quiz_testing_method)
            quizQuestionScrollView.isVisible = true
            variant1Button.setText(R.string.answer_example_quiz_testing_method_variant1)
            variant2Button.setText(R.string.answer_example_quiz_testing_method_variant2)
            variant3Button.setText(R.string.answer_example_quiz_testing_method_variant3)
            variant4Button.setText(R.string.answer_example_quiz_testing_method_variant4)

            variant1Button.setOnClickListener { onVariantSelected(variant1Button, variant1Frame) }
            variant2Button.setOnClickListener { onVariantSelected(variant2Button, variant2Frame) }
            variant3Button.setOnClickListener { onVariantSelected(variant3Button, variant3Frame) }
            variant4Button.setOnClickListener { onVariantSelected(variant4Button, variant4Frame) }
        }
    }

    private fun onVariantSelected(selectedButton: TextView, selectedFrame: FrameLayout) {
        variant1Button.isClickable = false
        variant2Button.isClickable = false
        variant3Button.isClickable = false
        variant4Button.isClickable = false

        if (selectedButton == variant4Button) {
            ExerciseHelpArticleFragment.makeVariantCorrect(selectedFrame, selectedButton)
        } else {
            ExerciseHelpArticleFragment.makeVariantCorrectButNotSelected(variant4Frame)
            ExerciseHelpArticleFragment.makeVariantWrong(selectedFrame, selectedButton)
        }
    }

    private fun setupEntryTestExerciseCard() {
        val correctAnswer: String = getString(R.string.answer_example_entry_testing_method)
        with(entryTestExerciseCardExample) {
            entryQuestionTextView.setText(R.string.question_example_entry_testing_method)
            entryQuestionScrollView.isVisible = true
            answerEditText.isEnabled = true
            answerEditText.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) v.hideSoftInput()
            }
            correctAnswerTextView.text = correctAnswer
            checkButton.setOnClickListener {
                checkButton.isVisible = false
                answerEditText.isEnabled = false
                answerInputScrollView.isVisible = false
                entryAnswerScrollView.isVisible = true
                val userAnswer: String? = answerEditText.text?.toString()?.trim()
                if (userAnswer != correctAnswer) {
                    wrongAnswerTextView.run {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = if (userAnswer.isNullOrEmpty()) "--" else userAnswer
                        isVisible = true
                    }
                }
            }
        }
    }
}