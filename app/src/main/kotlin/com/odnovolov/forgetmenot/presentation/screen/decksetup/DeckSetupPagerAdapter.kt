package com.odnovolov.forgetmenot.presentation.screen.decksetup

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentFragment
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsFragment

class DeckSetupPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> {
            DeckSettingsDiScope.open { DeckSettingsDiScope.create(PresetDialogState()) }
            DeckSettingsFragment()
        }
        else -> {
            DeckContentDiScope.open { DeckContentDiScope() }
            DeckContentFragment()
        }
    }
}