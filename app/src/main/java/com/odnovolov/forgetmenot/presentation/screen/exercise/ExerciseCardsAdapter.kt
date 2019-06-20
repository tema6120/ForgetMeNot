package com.odnovolov.forgetmenot.presentation.screen.exercise

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.ViewState
import io.reactivex.functions.Consumer

class ExerciseCardsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), Consumer<ViewState> {

    private var items: List<ExerciseCard> = emptyList()

    override fun createFragment(position: Int): Fragment {
        val exerciseCard = items[position]
        return ExerciseCardFragment.create(exerciseCard.id)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return items.any { exerciseCard -> exerciseCard.id.toLong() == itemId }
    }

    override fun accept(viewState: ViewState) {
        items = viewState.exerciseCards
        notifyDataSetChanged()
    }

}