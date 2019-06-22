package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mvicorediff.modelWatcher
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.News.MoveToNextPosition
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent.*
import kotlinx.android.synthetic.main.fragment_exercise.*
import leakcanary.LeakSentry
import javax.inject.Inject

class ExerciseFragment : BaseFragment<ViewState, UiEvent, News>() {

    @Inject lateinit var bindings: ExerciseFragmentBindings
    @Inject lateinit var adapter: ExerciseCardsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Injector.inject(this)
        bindings.setup(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPagerAdapter()
        setupControlPanel()
    }

    private fun setupControlPanel() {
        notAskButton.setOnClickListener { emitEvent(NotAskButtonClick) }
        undoButton.setOnClickListener { emitEvent(UndoButtonClick) }
    }

    private fun setupViewPagerAdapter() {
        exerciseViewPager.adapter = adapter
        exerciseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                emitEvent(NewPageBecomesSelected(position))
            }
        })
    }

    override fun accept(viewState: ViewState) {
        val currentPosition = viewState.currentPosition ?: return
        val currentExerciseCard = viewState.exerciseCards[currentPosition]
        watcher.invoke(currentExerciseCard)
    }

    private val watcher = modelWatcher<ExerciseCard> {
        watch({it.card.isLearned}) { isLearned ->
            if (isLearned) {
                notAskButton.visibility = View.GONE
                undoButton.visibility = View.VISIBLE
            } else {
                notAskButton.visibility = View.VISIBLE
                undoButton.visibility = View.GONE
            }
        }
    }

    override fun acceptNews(news: News) {
        when (news) {
            is MoveToNextPosition -> scrollViewPagerToNextPosition()
        }
    }

    private fun scrollViewPagerToNextPosition() {
        val nextPosition = exerciseViewPager.currentItem + 1
        exerciseViewPager.setCurrentItem(nextPosition, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }
}