package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import kotlinx.android.synthetic.main.article_deck_summary.*

class DeckSummaryHelpArticleFragment : BaseFragment() {
    private class State : FlowableState<State>() {
        var isSelected: Boolean by me(false)
    }

    private val state = State()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_deck_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deckButton.setOnClickListener {
            if (state.isSelected) state.isSelected = false
        }
        deckButton.setOnLongClickListener {
            state.isSelected = !state.isSelected
            true
        }
        state.flowOf(State::isSelected).observe(deckButton::setSelected)
    }
}