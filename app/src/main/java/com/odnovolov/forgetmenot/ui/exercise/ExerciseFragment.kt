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
            isNotAskButtonVisible.observe(viewLifecycleOwner, Observer { isVisible ->
                notAskButton.visibility = if (isVisible) View.VISIBLE else View.GONE
            })
            isUndoButtonVisible.observe(viewLifecycleOwner, Observer { isVisible ->
                undoButton.visibility = if (isVisible) View.VISIBLE else View.GONE
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

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }
}