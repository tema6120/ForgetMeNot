package com.odnovolov.forgetmenot.presentation.common

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

interface Store {
    fun loadGlobalState(): GlobalState
    fun loadDeckReviewPreference(): DeckReviewPreference
    fun loadWalkingModePreference(): WalkingModePreference
    fun saveStateByRegistry()
}