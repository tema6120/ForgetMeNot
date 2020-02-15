package com.odnovolov.forgetmenot.presentation.screen.exercise

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardFragment

class ExerciseCardsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    var exerciseCardIds: List<Long> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun createFragment(position: Int): Fragment {
        val id = exerciseCardIds[position]
        return ExerciseCardFragment.create(id)
    }

    override fun getItemId(position: Int): Long = exerciseCardIds[position]

    override fun containsItem(itemId: Long): Boolean = exerciseCardIds.contains(itemId)

    override fun getItemCount(): Int = exerciseCardIds.size
}