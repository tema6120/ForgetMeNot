package com.odnovolov.forgetmenot.presentation.screen.decksetup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import kotlinx.android.synthetic.main.fragment_deck_overview.*

class DeckSetupFragment : Fragment() {
    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deckOverviewViewPager.adapter = DeckSetupPagerAdapter(this)
        tabLayoutMediator = TabLayoutMediator(
            deckOverviewTabLayout,
            deckOverviewViewPager
        ) { tab, position ->
            tab.text = getString(
                when (position) {
                    0 -> R.string.tab_name_settings
                    1 -> R.string.tab_name_content
                    else -> throw IllegalArgumentException("position must be in 0..1")
                }
            )
        }.apply { attach() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        deckOverviewViewPager.adapter = null
    }
}