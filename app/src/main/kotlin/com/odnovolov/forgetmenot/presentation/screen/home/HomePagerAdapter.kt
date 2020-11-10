package com.odnovolov.forgetmenot.presentation.screen.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    var isFoundCardsFragmentEnabled = false
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun getItemCount(): Int = if (isFoundCardsFragmentEnabled) 2 else 1

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> DeckListFragment()
        else -> FoundCardsFragment()
    }
}