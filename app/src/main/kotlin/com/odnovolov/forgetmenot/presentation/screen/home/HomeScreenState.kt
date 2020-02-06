package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class HomeScreenState : FlowableState<HomeScreenState>() {
    var searchText: String by me("")
    var selectedDeckIds: List<Long> by me(emptyList())
}