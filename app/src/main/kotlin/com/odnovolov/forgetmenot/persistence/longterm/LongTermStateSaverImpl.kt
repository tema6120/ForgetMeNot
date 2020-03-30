package com.odnovolov.forgetmenot.persistence.longterm

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges.*
import com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference.WalkingModePreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

object LongTermStateSaverImpl : LongTermStateSaver {
    private val dbDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun saveStateByRegistry() {
        val changes = PropertyChangeRegistry.removeAll()
        if (changes.isEmpty()) return
        GlobalScope.launch(dbDispatcher) {
            database.transaction {
                changes.forEach(LongTermStateSaverImpl::save)
            }
        }
    }

    private fun save(change: Change) {
        when (change.propertyOwnerClass) {
            GlobalState::class -> GlobalStatePropertyChangeHandler.handle(change)
            Deck::class -> DeckPropertyChangeHandler.handle(change)
            Card::class -> CardPropertyChangeHandler.handle(change)
            ExercisePreference::class -> ExercisePreferencePropertyChangeHandler.handle(change)
            IntervalScheme::class -> IntervalSchemePropertyChangeHandler.handle(change)
            Interval::class -> IntervalPropertyChangeHandler.handle(change)
            Pronunciation::class -> PronunciationPropertyChangeHandler.handle(change)
            RepetitionSetting::class -> RepetitionSettingPropertyChangeHandler.handle(change)
            DeckReviewPreference::class -> DeckReviewPreferencePropertyChangeHandler.handle(change)
            WalkingModePreference::class -> WalkingModePreferencePropertyChangeHandler.handle(change)
        }
    }
}