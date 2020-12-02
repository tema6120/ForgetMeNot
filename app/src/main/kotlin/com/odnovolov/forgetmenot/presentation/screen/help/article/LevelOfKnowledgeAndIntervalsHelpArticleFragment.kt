package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.graphics.Paint
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.getBackgroundResForGrade
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseToDemonstrateCardsRetesting.Card
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseToDemonstrateCardsRetesting.ExerciseCard
import kotlinx.android.synthetic.main.article_level_of_knowledge_and_intervals.*
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseToDemonstrateCardsRetesting as ExampleExercise

class LevelOfKnowledgeAndIntervalsHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    override val layoutRes: Int get() = R.layout.article_level_of_knowledge_and_intervals
    override val helpArticle: HelpArticle get() = HelpArticle.LevelOfKnowledgeAndIntervals
    private val exercise by lazy(::createExercise)
    private val intervalsAdapter: IntervalsAdapter by lazy(::createIntervalsAdapter)
    private val levelOfKnowledgePopup: PopupWindow by lazy(::createLevelOfKnowledgePopup)

    override fun setupView() {
        val adapter = ExampleExerciseCardAdapter(viewCoroutineScope!!, exercise)
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = adapter
        gradeButton.run {
            setOnClickListener { showLevelOfKnowledgePopup() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        exercise.state.flowOf(ExampleExercise.State::exerciseCards)
            .observe(adapter::submitList)
        exercise.state.exerciseCards[0].card.flowOf(Card::levelOfKnowledge)
            .observe { levelOfKnowledge: Int ->
                val backgroundRes = getBackgroundResForGrade(levelOfKnowledge)
                gradeTextView.setBackgroundResource(backgroundRes)
                gradeTextView.text = levelOfKnowledge.toString()
            }
        exercise.state.exerciseCards[0].card.flowOf(Card::isLevelOfKnowledgeEditedManually)
            .observe { isEdited: Boolean ->
                with(gradeTextView) {
                    if (isEdited) {
                        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        setTypeface(null, Typeface.BOLD)
                    } else {
                        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                        setTypeface(null, Typeface.NORMAL)
                    }
                }
            }
    }

    private fun createExercise(): ExampleExercise {
        val card = Card(
            id = generateId(),
            question = getString(R.string.question_in_level_of_knowledge_and_intervals_article),
            answer = getString(R.string.answer_in_level_of_knowledge_and_intervals_article),
            levelOfKnowledge = 3
        )
        val exerciseCard = ExerciseCard(
            id = generateId(),
            card = card
        )
        return ExampleExercise(
            state = ExampleExercise.State(listOf(exerciseCard))
        )
    }

    private fun createIntervalsAdapter(): IntervalsAdapter {
        val onItemClick: (Int) -> Unit = { levelOfKnowledge: Int ->
            exercise.setLevelOfKnowledge(levelOfKnowledge, exercise.state.exerciseCards.first())
            levelOfKnowledgePopup.dismiss()
        }
        return IntervalsAdapter(onItemClick)
    }

    private fun createLevelOfKnowledgePopup(): PopupWindow {
        val recycler: RecyclerView =
            View.inflate(context, R.layout.popup_set_grade, null) as RecyclerView
        recycler.adapter = intervalsAdapter
        return PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = recycler
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
        }
    }

    private fun showLevelOfKnowledgePopup() {
        val currentLevelOfKnowledge = exercise.state.exerciseCards.first().card.levelOfKnowledge
        intervalsAdapter.intervalItems = IntervalScheme.Default.intervals
            .map { interval: Interval ->
                IntervalItem(
                    grade = interval.grade,
                    waitingPeriod = interval.value,
                    isSelected = currentLevelOfKnowledge == interval.grade
                )
            }
        val content = levelOfKnowledgePopup.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        gradeButton.getLocationOnScreen(location)
        val x = location[0] + 8.dp
        val y = location[1] + gradeButton.height - 8.dp - content.measuredHeight
        levelOfKnowledgePopup.showAtLocation(
            gradeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }
}