package com.odnovolov.forgetmenot.presentation.screen.fileimport

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cards.ImportedCardsDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cards.ImportedCardsFragment
import com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext.ImportedTextEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext.ImportedTextEditorFragment

class FileImportPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> {
            ImportedTextEditorDiScope.open { ImportedTextEditorDiScope() }
            ImportedTextEditorFragment()
        }
        else -> {
            ImportedCardsDiScope.open { ImportedCardsDiScope() }
            ImportedCardsFragment()
        }
    }
}