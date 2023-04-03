package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards.ImportedCardsFragment
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.ImportedTextEditorFragment

class CardsFileTabPagerAdapter(
    private val id: Long,
    fragment: Fragment
) : FragmentStateAdapter(
    fragment
) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ImportedCardsFragment.create(id)
        else -> ImportedTextEditorFragment.create(id)
    }
}