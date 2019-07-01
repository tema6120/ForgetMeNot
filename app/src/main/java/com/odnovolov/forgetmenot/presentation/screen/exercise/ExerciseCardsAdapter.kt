package com.odnovolov.forgetmenot.presentation.screen.exercise

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.ViewState
import io.reactivex.functions.Consumer

class ExerciseCardsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), Consumer<ViewState> {

    private var itemCount: Int = 0

    override fun createFragment(position: Int): Fragment {
        return ExerciseCardFragment.create(position)
    }

    override fun getItemCount() = itemCount

    override fun accept(viewState: ViewState) {
        itemCount = viewState.exerciseCards.size
        notifyDataSetChanged()
    }
}