package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.color
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.getBackgroundResForLevelOfKnowledge
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.LevelOfKnowledgeAndIntervals
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleOpened
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseToDemonstrateCardsRetesting.Card
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseToDemonstrateCardsRetesting.ExerciseCard
import kotlinx.android.synthetic.main.article_level_of_knowledge_and_intervals.*
import kotlinx.coroutines.launch

class LevelOfKnowledgeAndIntervalsHelpArticleFragment : BaseFragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    private val exercise by lazy {
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
        ExampleExerciseToDemonstrateCardsRetesting(
            state = ExampleExerciseToDemonstrateCardsRetesting.State(listOf(exerciseCard))
        )
    }
    private val intervalsAdapter: IntervalsAdapter by lazy(::createIntervalsAdapter)
    private val levelOfKnowledgePopup: PopupWindow by lazy(::createLevelOfKnowledgePopup)

    private fun createIntervalsAdapter(): IntervalsAdapter {
        val onItemClick: (Int) -> Unit = { levelOfKnowledge: Int ->
            exercise.setLevelOfKnowledge(levelOfKnowledge, exercise.state.exerciseCards.first())
            levelOfKnowledgePopup.dismiss()
        }
        return IntervalsAdapter(onItemClick)
    }

    private fun createLevelOfKnowledgePopup(): PopupWindow {
        val recycler: RecyclerView =
            View.inflate(context, R.layout.popup_set_level_of_knowledge, null) as RecyclerView
        recycler.adapter = intervalsAdapter
        return PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = recycler
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_level_of_knowledge_and_intervals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ExampleExerciseCardAdapter(viewCoroutineScope!!, exercise)
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = adapter
        levelOfKnowledgeButton.run {
            setOnClickListener { showLevelOfKnowledgePopup() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        exercise.state.flowOf(ExampleExerciseToDemonstrateCardsRetesting.State::exerciseCards)
            .observe(adapter::submitList)
        exercise.state.exerciseCards[0].card.flowOf(Card::levelOfKnowledge)
            .observe { levelOfKnowledge: Int ->
                val backgroundRes = getBackgroundResForLevelOfKnowledge(levelOfKnowledge)
                levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
                levelOfKnowledgeTextView.text = levelOfKnowledge.toString()
            }
        exercise.state.exerciseCards[0].card.flowOf(Card::isLevelOfKnowledgeEditedManually)
            .observe { isEdited: Boolean ->
                with(levelOfKnowledgeTextView) {
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

    private fun showLevelOfKnowledgePopup() {
        val currentLevelOfKnowledge = exercise.state.exerciseCards.first().card.levelOfKnowledge
        intervalsAdapter.intervalItems = IntervalScheme.Default.intervals
            .map { interval: Interval ->
                IntervalItem(
                    levelOfKnowledge = interval.levelOfKnowledge,
                    waitingPeriod = interval.value,
                    isSelected = currentLevelOfKnowledge == interval.levelOfKnowledge
                )
            }
        val content = levelOfKnowledgePopup.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        levelOfKnowledgeButton.getLocationOnScreen(location)
        val x = location[0] + 8.dp
        val y = location[1] + levelOfKnowledgeButton.height - 8.dp - content.measuredHeight
        levelOfKnowledgePopup.showAtLocation(
            levelOfKnowledgeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(ArticleOpened(LevelOfKnowledgeAndIntervals))
        }
    }
}