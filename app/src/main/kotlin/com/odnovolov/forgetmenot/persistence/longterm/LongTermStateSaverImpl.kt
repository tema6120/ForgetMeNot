package com.odnovolov.forgetmenot.persistence.longterm

import android.util.Log
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges.*
import com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference.WalkingModePreferencePropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class LongTermStateSaverImpl(
    private val database: Database
) : LongTermStateSaver {
    private val propertyChangeHandlers: Map<KClass<*>, PropertyChangeHandler> =
        HashMap<KClass<*>, PropertyChangeHandler>().apply {
            val intervalSchemePropertyChangeHandler = IntervalSchemePropertyChangeHandler(database)
            val speakPlanPropertyChangeHandler = SpeakPlanPropertyChangeHandler(database)
            val exercisePreferencePropertyChangeHandler = ExercisePreferencePropertyChangeHandler(
                database,
                intervalSchemePropertyChangeHandler,
                speakPlanPropertyChangeHandler
            )
            val deckPropertyChangeHandler = DeckPropertyChangeHandler(
                database,
                exercisePreferencePropertyChangeHandler
            )
            val globalStatePropertyChangeHandler = GlobalStatePropertyChangeHandler(
                database,
                deckPropertyChangeHandler,
                exercisePreferencePropertyChangeHandler
            )

            put(GlobalState::class, globalStatePropertyChangeHandler)
            put(Deck::class, deckPropertyChangeHandler)
            put(Card::class, CardPropertyChangeHandler(database))
            put(ExercisePreference::class, exercisePreferencePropertyChangeHandler)
            put(IntervalScheme::class, IntervalSchemePropertyChangeHandler(database))
            put(Interval::class, IntervalPropertyChangeHandler(database))
            put(Pronunciation::class, PronunciationPropertyChangeHandler(database))
            put(SpeakPlan::class, speakPlanPropertyChangeHandler)
            put(RepetitionSetting::class, RepetitionSettingPropertyChangeHandler(database))
            put(DeckReviewPreference::class, DeckReviewPreferencePropertyChangeHandler(database))
            put(WalkingModePreference::class, WalkingModePreferencePropertyChangeHandler(database))
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