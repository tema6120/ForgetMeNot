package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import android.graphics.Paint
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent.ExampleExerciseToDemonstrateCardsRetesting.Card
import com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent.ExampleExerciseToDemonstrateCardsRetesting.ExerciseCard
import kotlinx.android.synthetic.main.article_grade_and_intervals.*
import kotlinx.android.synthetic.main.popup_intervals.view.*
import com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent.ExampleExerciseToDemonstrateCardsRetesting as ExampleExercise

class GradesAndIntervalsHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    override val layoutRes: Int get() = R.layout.article_grade_and_intervals
    override val helpArticle: HelpArticle get() = HelpArticle.GradesAndIntervals
    private val exercise by lazy(::createExercise)
    private val intervalsAdapter: IntervalsAdapter by lazy(::createIntervalsAdapter)
    private val intervalsPopup: PopupWindow by lazy(::createIntervalsPopup)

    override fun setupView() {
        val adapter = ExampleExerciseCardAdapter(viewCoroutineScope!!, exercise)
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = adapter
        gradeButton.run {
            setOnClickListener { showIntervalsPopup() }
            setTooltipTextFromContentDescription()
        }
        exercise.state.flowOf(ExampleExercise.State::exerciseCards)
            .observe(adapter::submitList)
        exercise.state.exerciseCards[0].card.flowOf(Card::grade)
            .observe { grade: Int ->
                gradeButton.text = grade.toString()
                val gradeColorRes = getGradeColorRes(grade)
                gradeButton.setBackgroundTintFromRes(gradeColorRes)
            }
        exercise.state.exerciseCards[0].card.flowOf(Card::isGradeEditedManually)
            .observe { isEdited: Boolean ->
                with(gradeButton) {
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
            question = getString(R.string.question_in_grade_and_intervals_article),
            answer = getString(R.string.answer_in_grade_and_intervals_article),
            grade = 3
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
        val onItemClick: (Int) -> Unit = { grade: Int ->
            exercise.setGrade(grade, exercise.state.exerciseCards.first())
            intervalsPopup.dismiss()
        }
        return IntervalsAdapter(onItemClick)
    }

    private fun createIntervalsPopup(): PopupWindow {
        val content = View.inflate(context, R.layout.popup_intervals, null).apply {
            intervalsRecycler.adapter = intervalsAdapter
        }
        return DarkPopupWindow(content)
    }

    private fun showIntervalsPopup() {
        val currentGrade = exercise.state.exerciseCards.first().card.grade
        intervalsAdapter.intervalItems = IntervalScheme.Default.intervals
            .map { interval: Interval ->
                IntervalItem(
                    grade = interval.grade,
                    waitingPeriod = interval.value,
                    isSelected = currentGrade == interval.grade
                )
            }
        intervalsPopup.show(anchor = gradeButton, Gravity.BOTTOM)
    }
}