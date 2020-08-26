package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import kotlinx.android.synthetic.main.article_test_methods.*
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.answerScrollView as entryAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.answerScrollView as manualAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.answerTextView as manualAnswerTextView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.answerScrollView as offAnswerScrollView
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.answerTextView as offAnswerTextView

class TestMethodsArticleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_test_methods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOffTestExerciseCard()
        setupManualTestExerciseCard()
        setupQuizTextExerciseCard()
        setupEntryTestExerciseCard()
    }

    private fun setupOffTestExerciseCard() {
        with(offTestExerciseCardExample) {
            questionTextView.setText(R.string.question_example_off_test_method)
            offAnswerTextView.setText(R.string.answer_example_off_test_method)
            showAnswerButton.setOnClickListener {
                showAnswerButton.visibility = View.GONE
                offAnswerScrollView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupManualTestExerciseCard() {
        with(manualTestExerciseCardExample) {
            questionTextView.setText(R.string.question_example_manual_test_method)
            manualAnswerTextView.setText(R.string.answer_example_manual_test_method)
            rememberButton.setOnClickListener {
                manualAnswerScrollView.visibility = View.VISIBLE
                rememberButton.isSelected = true
                notRememberButton.isSelected = false
            }
            notRememberButton.setOnClickListener {
                manualAnswerScrollView.visibility = View.VISIBLE
                rememberButton.isSelected = false
                notRememberButton.isSelected = true
            }
        }
    }

    private fun setupQuizTextExerciseCard() {
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
                answerEditText.visibility = View.GONE
                answerEditText.isEnabled = false
                checkButton.visibility = View.GONE
                checkDivider.visibility = View.GONE
                entryAnswerScrollView.visibility = View.VISIBLE
                val userAnswer: String? = answerEditText.text?.toString()?.trim()
                if (userAnswer != correctAnswer) {
                    wrongAnswerTextView.run {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        text = userAnswer
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}