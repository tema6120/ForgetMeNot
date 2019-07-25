package com.odnovolov.forgetmenot.ui.exercise

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter

class ExerciseCardsAdapter(
    fragment: Fragment,
    viewModel: ExerciseViewModel
) : FragmentStateAdapter(fragment) {

    init {
        viewModel.state.exerciseCards.observe(fragment, Observer { exerciseCards ->
            itemCount = exerciseCards?.size ?: 0
            notifyDataSetChanged()
        })
    }

    private var itemCount: Int = 0

    override fun createFragment(position: Int): Fragment {
        return ExerciseCardFragment.create(position)
    }

    override fun getItemCount(): Int = itemCount
}