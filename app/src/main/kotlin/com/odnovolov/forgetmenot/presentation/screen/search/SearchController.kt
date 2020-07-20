package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.*

class SearchController(
    private val screenState: SearchScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val searchScreenStateProvider: ShortTermStateProvider<SearchScreenState>
) : BaseController<SearchEvent, Nothing>() {
    override fun handle(event: SearchEvent) {
        when (event) {
            BackButtonClicked -> {
                navigator.navigateUp()
            }

            is SearchTextChanged -> {
                screenState.searchText = event.text
            }

            is CardClicked -> {

            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        searchScreenStateProvider.save(screenState)
    }
}