package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toCardDb
import com.odnovolov.forgetmenot.persistence.toDeckDb
import com.odnovolov.forgetmenot.persistence.toRepetitionSettingDb

class GlobalStatePropertyChangeHandler(
    private val database: Database,
    private val deckPropertyChangeHandler: DeckPropertyChangeHandler,
    private val exercisePreferencePropertyChangeHandler: ExercisePreferencePropertyChangeHandler
) : PropertyChangeHandler {
    override fun handle(change: PropertyChangeRegistry.Change) {
        when (change.property) {
            GlobalState::decks -> {
                if (change !is CollectionChange) return

                val removedDecks = change.removedItems as Collection<Deck>
                removedDecks.forEach { deck: Deck ->
                    database.deckQueries.delete(deck.id)
                }

                val addedDecks = change.addedItems as Collection<Deck>
                addedDecks.forEach { deck ->
                    val deckDb = deck.toDeckDb()
                    database.deckQueries.insert(deckDb)
                    deck.cards.mapIndexed { index, card -> card.toCardDb(deck.id, ordinal = index) }
                        .forEach(database.cardQueries::insert)
                    deckPropertyChangeHandler.insertExercisePreferenceIfNotExists(
                        deck.exercisePreference
                    )
                }
            }
            GlobalState::sharedExercisePreferences -> {
                if (change !is CollectionChange) return

                val removedSharedExercisePreferences =
                    change.removedItems as Collection<ExercisePreference>
                removedSharedExercisePreferences.forEach { exercisePreference: ExercisePreference ->
                    database.sharedExercisePreferenceQueries.delete(exercisePreference.id)
                }

                val addedSharedExercisePreferences =
                    change.addedItems as Collection<ExercisePreference>
                addedSharedExercisePreferences.forEach { exercisePreference: ExercisePreference ->
                    database.sharedExercisePreferenceQueries.insert(exercisePreference.id)
                    deckPropertyChangeHandler.insertExercisePreferenceIfNotExists(exercisePreference)
                }
            }
            GlobalState::sharedIntervalSchemes -> {
                if (change !is CollectionChange) return

                val removedSharedIntervalSchemes = change.removedItems as Collection<IntervalScheme>
                removedSharedIntervalSchemes.forEach { intervalScheme: IntervalScheme ->
                    database.sharedIntervalSchemeQueries.delete(intervalScheme.id)
                }

                val addedSharedIntervalSchemes = change.addedItems as Collection<IntervalScheme>
                addedSharedIntervalSchemes.forEach { intervalScheme: IntervalScheme ->
                    database.sharedIntervalSchemeQueries.insert(intervalScheme.id)
                    exercisePreferencePropertyChangeHandler.insertIntervalSchemeIfNotExists(
                        intervalScheme
                    )
                }
            }
            GlobalState::sharedPronunciations -> {
                if (change !is CollectionChange) return

                val removedSharedPronunciations = change.removedItems as Collection<Pronunciation>
                removedSharedPronunciations.forEach { pronunciation: Pronunciation ->
                    database.sharedPronunciationQueries.delete(pronunciation.id)
                }

                val addedSharedPronunciations = change.addedItems as Collection<Pronunciation>
                addedSharedPronunciations.forEach { pronunciation: Pronunciation ->
                    database.sharedPronunciationQueries.insert(pronunciation.id)
                    exercisePreferencePropertyChangeHandler.insertPronunciationIfNotExists(
                        pronunciation
                    )
                }
            }
            GlobalState::sharedPronunciationPlans -> {
                if (change !is CollectionChange) return

                val removedSharedPronunciationPlans =
                    change.removedItems as Collection<PronunciationPlan>
                removedSharedPronunciationPlans.forEach { pronunciationPlan: PronunciationPlan ->
                    database.sharedPronunciationPlanQueries.delete(pronunciationPlan.id)
                }

                val addedSharedPronunciationPlans =
                    change.addedItems as Collection<PronunciationPlan>
                addedSharedPronunciationPlans.forEach { pronunciationPlan: PronunciationPlan ->
                    database.sharedPronunciationPlanQueries.insert(pronunciationPlan.id)
                    exercisePreferencePropertyChangeHandler
                        .insertPronunciationPlanIfNotExists(pronunciationPlan)
                }
            }
            GlobalState::sharedRepetitionSettings -> {
                if (change !is CollectionChange) return

                val removedRepetitionSettings = change.removedItems as Collection<RepetitionSetting>
                removedRepetitionSettings.forEach { repetitionSetting: RepetitionSetting ->
                    database.sharedRepetitionSettingQueries.delete(repetitionSetting.id)
                }

                val addedRepetitionSettings = change.addedItems as Collection<RepetitionSetting>
                addedRepetitionSettings.forEach { repetitionSetting: RepetitionSetting ->
                    insertRepetitionSettingIfNotExists(repetitionSetting)
                    database.sharedRepetitionSettingQueries.insert(repetitionSetting.id)
                }
            }
            GlobalState::currentRepetitionSetting -> {
                if (change !is PropertyValueChange) return
                val currentRepetitionSetting = change.newValue as RepetitionSetting
                insertRepetitionSettingIfNotExists(currentRepetitionSetting)
                database.currentRepetitionSettingQueries.update(currentRepetitionSetting.id)
            }
            GlobalState::isWalkingModeEnabled -> {
                if (change !is PropertyValueChange) return
                val isWalkingModeEnabled = change.newValue as Boolean
                database.walkingModeQueries.updateIsEnabled(isWalkingModeEnabled)
            }
        }
    }

    private fun insertRepetitionSettingIfNotExists(repetitionSetting: RepetitionSetting) {
        val exists: Boolean = repetitionSetting.isDefault()
                || database.repetitionSettingQueries.exists(repetitionSetting.id).executeAsOne()
        if (!exists) {
            val repetitionSettingDb = repetitionSetting.toRepetitionSettingDb()
            database.repetitionSettingQueries.insert(repetitionSettingDb)
        }
    }
}