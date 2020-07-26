package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard

sealed class SearchEvent {
    object BackButtonClicked : SearchEvent()
    class SearchTextChanged(val text: String) : SearchEvent()
    class CardClicked(val searchCard: SearchCard) : SearchEvent()
}