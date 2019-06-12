package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.BaseFragment
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment.ViewState
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment.UiEvent
import kotlinx.android.synthetic.main.fragment_exercise.*
import javax.inject.Inject

class ExerciseFragment : BaseFragment<ViewState, UiEvent, Nothing>() {

    data class ViewState(
        val exerciseCards: List<ExerciseCard>
    )

    sealed class UiEvent {
    }

    @Inject lateinit var bindings: ExerciseFragmentBindings
    private val viewPagerAdapter = ExerciseCardsAdapter()

    init {
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
        exerciseViewPager.adapter = viewPagerAdapter
    }

    override fun accept(viewState: ViewState) {
        viewPagerAdapter.submitList(viewState.exerciseCards)
    }
}