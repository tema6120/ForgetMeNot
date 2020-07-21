package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class SearchScreenState(
    searchText: String = ""
) : FlowableState<SearchScreenState>() {
    var searchText: String by me(searchText)
}