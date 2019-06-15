package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.BaseFragment
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent.NewPageBecomesSelected
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.ViewState
import kotlinx.android.synthetic.main.fragment_exercise.*
import javax.inject.Inject

class ExerciseFragment : BaseFragment<ViewState, UiEvent, Nothing>() {

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
        adapter.submitList(viewState.exerciseCards)
    }
}