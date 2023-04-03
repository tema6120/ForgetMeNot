package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile

// used to synchronize the scroll position of imported cards and source text
interface ControllingTheScrollPosition {
    // in percentage terms
    fun getScrollPosition(): Float
    fun scrollTo(scrollPercentage: Float)
}