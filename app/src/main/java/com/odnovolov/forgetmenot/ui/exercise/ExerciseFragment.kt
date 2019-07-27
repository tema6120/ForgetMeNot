package com.odnovolov.forgetmenot.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.ui.exercise.ExerciseViewModel.Action.MoveToNextPosition
import com.odnovolov.forgetmenot.ui.exercise.ExerciseViewModel.Event.*
import kotlinx.android.synthetic.main.fragment_exercise.*
import leakcanary.LeakSentry

class ExerciseFragment : Fragment() {

    private lateinit var viewModel: ExerciseViewModel
    private var savedViewPagerPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ExerciseInjector.viewModel(this)
        super.onCreate(savedInstanceState)
        savedViewPagerPosition = savedInstanceState?.getInt(STATE_VIEW_PAGER_POSITION)
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
        setupView()
        subscribeToViewModel()
    }

    private fun setupView() {
        setupViewPagerAdapter()
        setupControlPanel()
    }

    private fun setupViewPagerAdapter() {
        exerciseViewPager.adapter = ExerciseCardsAdapter(this, viewModel)
        exerciseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onEvent(NewPageBecomesSelected(position))
            }
        })
    }

    private fun setupControlPanel() {
        notAskButton.setOnClickListener { viewModel.onEvent(NotAskButtonClick) }
        undoButton.setOnClickListener { viewModel.onEvent(UndoButtonClick) }
    }

    private fun subscribeToViewModel() {
        with(viewModel.state) {
            exerciseCards.observe(viewLifecycleOwner, Observer {
                if (savedViewPagerPosition != null) {
                    exerciseViewPager.setCurrentItem(savedViewPagerPosition!!, false)
                    savedViewPagerPosition = null
                }
            })
            isCurrentCardLearned.observe(viewLifecycleOwner, Observer { isCurrentCardLearned ->
                isCurrentCardLearned ?: return@Observer
                notAskButton.visibility = if (isCurrentCardLearned) View.GONE else View.VISIBLE
                undoButton.visibility = if (isCurrentCardLearned) View.VISIBLE else View.GONE
            })
        }

        viewModel.action!!.observe(this, Observer { action ->
            when (action) {
                MoveToNextPosition -> {
                    val nextPosition = exerciseViewPager.currentItem + 1
                    exerciseViewPager.setCurrentItem(nextPosition, true)
                }
            }
        })
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is ExerciseCardFragment) {
            childFragment.viewModel = viewModel
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_VIEW_PAGER_POSITION, exerciseViewPager.currentItem)
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }

    companion object {
        const val STATE_VIEW_PAGER_POSITION = "viewPagerPosition"
    }
}