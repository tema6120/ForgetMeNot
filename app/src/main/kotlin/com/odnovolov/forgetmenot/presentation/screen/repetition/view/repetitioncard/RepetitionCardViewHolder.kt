package com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard.RepetitionCardEvent.*
import kotlinx.android.synthetic.main.item_repetition_card.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RepetitionCardViewHolder(
    itemView: View,
    private val coroutineScope: CoroutineScope,
    controller: RepetitionCardController
) : RecyclerView.ViewHolder(itemView) {
    private var observing: Job? = null

    init {
        with(itemView) {
            showQuestionButton.setOnClickListener { controller.dispatch(ShowQuestionButtonClicked) }
            showAnswerButton.setOnClickListener { controller.dispatch(ShowAnswerButtonClicked) }
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            answerTextView.observeSelectedText { selection: String ->
                controller.dispatch(AnswerTextSelectionChanged(selection))
            }
        }
    }

    fun bind(repetitionCard: RepetitionCard) {
        observing?.cancel()
        val viewModel = RepetitionCardViewModel(repetitionCard)
        observing = coroutineScope.launch {
            val coroutineScope = this
            with(viewModel) {
                with(itemView) {
                    isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                        showQuestionButton.isVisible = !isQuestionDisplayed
                        questionScrollView.isVisible = isQuestionDisplayed
                    }
                    question.observe(coroutineScope) { question: String ->
                        questionTextView.text = question
                        questionTextView.fixTextSelection()
                    }
                    isAnswered.observe(coroutineScope) { isAnswered: Boolean ->
                        showAnswerButton.isVisible = !isAnswered
                        answerScrollView.isVisible = isAnswered
                    }
                    answer.observe(coroutineScope) { answer: String ->
                        answerTextView.text = answer
                        answerTextView.fixTextSelection()
                    }
                    isLearned.observe(coroutineScope) { isLearned: Boolean ->
                        val isEnabled = !isLearned
                        showQuestionButton.isEnabled = isEnabled
                        questionTextView.isEnabled = isEnabled
                        showAnswerButton.isEnabled = isEnabled
                        answerTextView.isEnabled = isEnabled
                    }
                    questionScrollView.scrollTo(0, 0)
                    answerScrollView.scrollTo(0, 0)
                }
            }
        }
    }
}