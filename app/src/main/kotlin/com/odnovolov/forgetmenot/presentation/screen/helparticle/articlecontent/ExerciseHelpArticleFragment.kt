package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import android.animation.AnimatorInflater
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.Prompter
import com.odnovolov.forgetmenot.presentation.common.DarkPopupWindow
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import com.odnovolov.forgetmenot.presentation.common.setTooltipTextFromContentDescription
import com.odnovolov.forgetmenot.presentation.common.show
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleEvent.ArticleLinkClicked
import com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent.ExampleExerciseToDemonstrateCardsRetesting.Card
import com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent.ExampleExerciseToDemonstrateCardsRetesting.ExerciseCard
import kotlinx.android.synthetic.main.article_exercise.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.*
import kotlinx.android.synthetic.main.popup_hints.view.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent.ExampleExerciseToDemonstrateCardsRetesting as ExampleExercise

class ExerciseHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    private class Example2State : FlowMaker<Example2State>() {
        var isLearned by flowMaker(false)
    }

    private class Example3State : FlowMaker<Example3State>() {
        var hint: String? by flowMaker(null)
        var hasHintSelection: Boolean by flowMaker(false)
    }

    override val layoutRes: Int get() = R.layout.article_exercise
    override val helpArticle: HelpArticle get() = HelpArticle.Exercise
    private val example2State = Example2State()
    private val example3State = Example3State()
    private val exercise by lazy(::createExercise)
    private var hintPopup: PopupWindow? = null

    private fun createExercise(): ExampleExercise {
        fun createExerciseCard(questionId: Int, answerId: Int): ExerciseCard {
            val card = Card(
                id = generateId(),
                question = getString(questionId),
                answer = getString(answerId),
                grade = 3
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
        return ExampleExercise(
            state = ExampleExercise.State(exerciseCards)
        )
    }

    override fun setupView() {
        setupText()
        setupExampleOfRetestingCards()
        setupExampleOfLearnedCard()
        setupExampleOfHints()
    }

    private fun setupText() {
        paragraph1.setTextWithClickableAnnotations(
            stringId = R.string.article_exercise_paragraph_1,
            onAnnotationClick = ::dispatchArticleLinkClicked
        )
        paragraph2.setTextWithClickableAnnotations(
            stringId = R.string.article_exercise_paragraph_2,
            onAnnotationClick = ::dispatchArticleLinkClicked
        )
        paragraph4.setTextWithClickableAnnotations(
            stringId = R.string.article_exercise_paragraph_4,
            onAnnotationClick = ::dispatchArticleLinkClicked
        )
    }

    private fun dispatchArticleLinkClicked(annotationValue: String) {
        controller?.dispatch(
            ArticleLinkClicked(
                when (annotationValue) {
                    "testing_method" -> HelpArticle.TestingMethods
                    "question_display" -> HelpArticle.QuestionDisplay
                    "pronunciation" -> HelpArticle.Pronunciation
                    "motivational_timer" -> HelpArticle.MotivationalTimer
                    "grade" -> HelpArticle.GradeAndIntervals
                    "walking_mode" -> HelpArticle.WalkingMode
                    else -> return
                }
            )
        )
    }

    private fun setupExampleOfRetestingCards() {
        val adapter = ExampleExerciseCardAdapter(viewCoroutineScope!!, exercise)
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = adapter
        exercise.state.flowOf(ExampleExercise.State::exerciseCards).observe(adapter::submitList)
    }

    private fun setupExampleOfLearnedCard() {
        with(learnedCardExample) {
            questionTextView.setText(R.string.question4_in_exercise_article)
            questionScrollView.isVisible = true
            answerTextView.setText(R.string.answer4_in_exercise_article)
            showAnswerButton.isVisible = false
            answerScrollView.isVisible = true
            markAsLearnedButton.setOnClickListener {
                example2State.isLearned = !example2State.isLearned
            }
            with(cardLabelTextView) {
                stateListAnimator =
                    AnimatorInflater.loadStateListAnimator(context, R.animator.card_label)
            }
            example2State.flowOf(Example2State::isLearned).observe { isLearned: Boolean ->
                with(markAsLearnedButton) {
                    setImageResource(
                        if (isLearned)
                            R.drawable.ic_mark_as_unlearned else
                            R.drawable.ic_mark_as_learned
                    )
                    contentDescription = getString(
                        if (isLearned)
                            R.string.description_mark_as_unlearned_button else
                            R.string.description_mark_as_learned_button
                    )
                    setTooltipTextFromContentDescription()
                }
                questionTextView.isEnabled = !isLearned
                answerTextView.isEnabled = !isLearned
                cardLabelTextView.isEnabled = isLearned
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
            questionScrollView.isVisible = true
            answerTextView.setText(R.string.answer5_in_exercise_article)
            hintButton.isActivated = true
            showAnswerButton.setOnClickListener {
                hintScrollView.isVisible = false
                hintDivider.isVisible = false
                showAnswerButton.isVisible = false
                answerFrame.updateLayoutParams<LinearLayout.LayoutParams> {
                    height = 0
                    weight = 0.5f
                }
                answerScrollView.isVisible = true
                hintButton.isVisible = false
            }
            hintButton.setOnClickListener {
                requireHintPopup().show(anchor = hintButton, Gravity.BOTTOM)
            }
            setTooltipTextFromContentDescription()
            example3State.flowOf(Example3State::hint).observe(hintTextView::setText)
            hintTextView.observeSelectedText { text: String ->
                val hasSelectedText = text.isNotEmpty()
                if (example3State.hasHintSelection != hasSelectedText) {
                    example3State.hasHintSelection = hasSelectedText
                }
            }
        }
    }

    private fun requireHintPopup(): PopupWindow {
        if (hintPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_hints, null).apply {
                hintIcon.isActivated = true
                getVariantsButton.setOnClickListener {
                    maskedLettersHintExample.isVisible = false
                    quizHintExample.isVisible = true
                    hintButton.isVisible = false
                    hintPopup?.dismiss()
                }
                maskLettersButton.setOnClickListener {
                    val answer: String = getString(R.string.answer5_in_exercise_article)
                    with(maskedLettersHintExample) {
                        when {
                            example3State.hint == null -> {
                                val maskedAnswer: String = Prompter.maskLetters(answer)
                                example3State.hint = maskedAnswer
                                answerFrame.updateLayoutParams<LinearLayout.LayoutParams> {
                                    height = WRAP_CONTENT
                                    weight = 0f
                                }
                                hintScrollView.updateLayoutParams<LinearLayout.LayoutParams> {
                                    height = 0
                                    weight = 0.5f
                                }
                                hintScrollView.isVisible = true
                                hintDivider.isVisible = true
                            }
                            example3State.hasHintSelection -> {
                                val startIndex =
                                    minOf(hintTextView.selectionStart, hintTextView.selectionEnd)
                                val endIndex =
                                    maxOf(hintTextView.selectionStart, hintTextView.selectionEnd)
                                example3State.hint = Prompter.unmaskRange(
                                    answer, example3State.hint!!,
                                    startIndex, endIndex
                                )
                            }
                            else -> {
                                example3State.hint =
                                    Prompter.unmaskFirst(answer, example3State.hint!!)
                            }
                        }
                    }
                    hintPopup?.dismiss()
                }
            }
            hintPopup = DarkPopupWindow(content)
            subscribeHintPopupToViewModel()
        }
        return hintPopup!!
    }

    private fun subscribeHintPopupToViewModel() {
        combine(
            example3State.flowOf(Example3State::hint),
            example3State.flowOf(Example3State::hasHintSelection)
        ) { hint: String?, hasHintSelection: Boolean ->
            hintPopup!!.contentView.run {
                maskLettersButton.setText(
                    when {
                        hint == null -> R.string.text_hint_mask_letters_button
                        hasHintSelection -> R.string.text_unmask_selected_region_button
                        else -> R.string.text_unmask_the_first_letter_button
                    }
                )
            }
        }.launchIn(viewCoroutineScope!!)
    }

    private fun setupQuizHintExample() {
        with(quizHintExample) {
            questionFrame.updateLayoutParams<LinearLayout.LayoutParams> {
                height = 0
                weight = 1f
            }
            variantsScrollView.updateLayoutParams<LinearLayout.LayoutParams> {
                height = WRAP_CONTENT
                weight = 0f
            }
            questionTextView.setText(R.string.question5_in_exercise_article)
            questionScrollView.isVisible = true
            variant1Button.setText(R.string.answer5_in_exercise_article)
            variant2Button.setText(R.string.variant2_in_exercise_article)
            variant3Button.setText(R.string.variant3_in_exercise_article)
            variant4Button.setText(R.string.variant4_in_exercise_article)

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

        if (selectedButton == variant1Button) {
            makeVariantCorrect(selectedFrame, selectedButton)
        } else {
            makeVariantCorrectButNotSelected(variant1Frame)
            makeVariantWrong(selectedFrame, selectedButton)
        }
    }

    companion object {
        fun makeVariantCorrect(variantFrame: FrameLayout, variantButton: TextView) {
            variantFrame.background = ContextCompat.getDrawable(
                variantFrame.context,
                R.drawable.background_variant_status_correct
            )
            variantButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_correct_answer_24, 0, 0, 0
            )
            val colorStateList = ContextCompat.getColorStateList(
                variantButton.context,
                R.color.icon_variant_status_correct
            )
            variantButton.setTextColor(colorStateList)
        }

        fun makeVariantCorrectButNotSelected(variantFrame: FrameLayout) {
            variantFrame.background = ContextCompat.getDrawable(
                variantFrame.context,
                R.drawable.background_variant_status_correct_but_not_selected
            )
        }

        fun makeVariantWrong(variantFrame: FrameLayout, variantButton: TextView) {
            variantFrame.background = ContextCompat.getDrawable(
                variantFrame.context,
                R.drawable.background_variant_status_wrong
            )
            variantButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_wrong_answer_24, 0, 0, 0
            )
            val colorStateList = ContextCompat.getColorStateList(
                variantButton.context,
                R.color.icon_variant_status_wrong
            )
            variantButton.setTextColor(colorStateList)
        }
    }
}