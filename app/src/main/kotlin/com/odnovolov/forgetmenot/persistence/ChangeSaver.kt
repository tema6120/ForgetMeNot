package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.deckreviewpreference.DeckReviewPreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference

object ChangeSaver {
    fun save(change: Change) {
        when (change.propertyOwnerClass) {
            GlobalState::class -> GlobalStatePropertyChangeHandler.handle(change)
            Deck::class -> DeckPropertyChangeHandler.handle(change)
            Card::class -> CardPropertyChangeHandler.handle(change)
            ExercisePreference::class -> ExercisePreferencePropertyChangeHandler.handle(change)
            IntervalScheme::class -> IntervalSchemePropertyChangeHandler.handle(change)
            Interval::class -> IntervalPropertyChangeHandler.handle(change)
            Pronunciation::class -> PronunciationPropertyChangeHandler.handle(change)
            DeckReviewPreference::class -> DeckReviewPreferencePropertyChangeHandler.handle(change)
        }
    }
}