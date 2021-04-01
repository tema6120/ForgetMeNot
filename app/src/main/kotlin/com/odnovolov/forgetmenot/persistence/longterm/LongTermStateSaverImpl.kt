package com.odnovolov.forgetmenot.persistence.longterm

import android.util.Log
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.autoplay.CardFilterForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.longterm.cardappearance.CardAppearancePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.exercisesettings.ExerciseSettingsPropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.fileimportstorage.FileFormatPropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.fileimportstorage.FileImportStoragePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.fullscreenpreference.FullscreenPreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges.*
import com.odnovolov.forgetmenot.persistence.longterm.initialdecksadderstate.InitialDecksAdderStatePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.lastusedlanguages.LastUsedLanguagesPropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.pronunciationpreference.PronunciationPreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.tipstate.TipStatePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference.WalkingModePreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LastUsedLanguages
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.TipState
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationPreference
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

class LongTermStateSaverImpl(
    private val database: Database,
    private val json: Json
) : LongTermStateSaver {
    private val propertyChangeHandlers: Map<KClass<*>, PropertyChangeHandler> =
        HashMap<KClass<*>, PropertyChangeHandler>().apply {
            val intervalSchemePropertyChangeHandler = IntervalSchemePropertyChangeHandler(database)
            val exercisePreferencePropertyChangeHandler = ExercisePreferencePropertyChangeHandler(
                database,
                intervalSchemePropertyChangeHandler
            )
            val deckPropertyChangeHandler = DeckPropertyChangeHandler(
                database,
                exercisePreferencePropertyChangeHandler
            )
            val globalStatePropertyChangeHandler = GlobalStatePropertyChangeHandler(
                database,
                deckPropertyChangeHandler
            )

            put(GlobalState::class, globalStatePropertyChangeHandler)
            put(Deck::class, deckPropertyChangeHandler)
            put(Card::class, CardPropertyChangeHandler(database))
            put(ExercisePreference::class, exercisePreferencePropertyChangeHandler)
            put(IntervalScheme::class, IntervalSchemePropertyChangeHandler(database))
            put(Interval::class, IntervalPropertyChangeHandler(database))
            put(Pronunciation::class, PronunciationPropertyChangeHandler(database))
            put(PronunciationPlan::class, PronunciationPlanPropertyChangeHandler(database))
            put(CardFilterForExercise::class, CardFilterForExerciseChangeHandler(database))
            put(CardFilterForAutoplay::class, CardFilterForAutoplayChangeHandler(database))
            put(DeckReviewPreference::class, DeckReviewPreferencePropertyChangeHandler(database))
            put(WalkingModePreference::class, WalkingModePreferencePropertyChangeHandler(database))
            put(FullscreenPreference::class, FullscreenPreferencePropertyChangeHandler(database))
            put(InitialDecksAdder.State::class, InitialDecksAdderStatePropertyChangeHandler(database))
            put(TipState::class, TipStatePropertyChangeHandler(database))
            put(FileImportStorage::class, FileImportStoragePropertyChangeHandler(database))
            put(FileFormat::class, FileFormatPropertyChangeHandler(database))
            put(PronunciationPreference::class, PronunciationPreferencePropertyChangeHandler(database))
            put(LastUsedLanguages::class, LastUsedLanguagesPropertyChangeHandler(database))
            put(CardAppearance::class, CardAppearancePropertyChangeHandler(database))
            put(DeckList::class, DeckListPropertyChangeHandler(database))
            put(ExerciseSettings::class, ExerciseSettingsPropertyChangeHandler(database, json))
        }

    override fun saveStateByRegistry() {
        val changes: List<PropertyChangeRegistry.Change> = PropertyChangeRegistry.removeAll()
        if (changes.isEmpty()) return
        GlobalScope.launch(Dispatchers.IO) {
            database.transaction {
                changes.forEach(::save)
            }
        }
    }

    private fun save(change: PropertyChangeRegistry.Change) {
        if (BuildConfig.DEBUG) {
            Log.d("db", change.toString())
        }
        val handler: PropertyChangeHandler? = propertyChangeHandlers[change.propertyOwnerClass]
        if (handler != null) {
            handler.handle(change)
        } else if (BuildConfig.DEBUG) {
            Log.w("db", "UNHANDLED CHANGE: $change")
        }
    }
}