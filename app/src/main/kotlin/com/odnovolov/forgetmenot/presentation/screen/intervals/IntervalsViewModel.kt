package com.odnovolov.forgetmenot.presentation.screen.intervals

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.*
import org.koin.java.KoinJavaComponent.getKoin

class IntervalsViewModel(
    deckSettingsState: DeckSettings.State
) : ViewModel() {
    val intervals: Flow<List<Interval>> = deckSettingsState.deck.flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::intervalScheme)
        }
        .flatMapLatest { intervalScheme: IntervalScheme? ->
            intervalScheme?.flowOf(IntervalScheme::intervals) ?: flowOf(emptyList<Interval>())
        }
        .flatMapLatest { intervals: List<Interval> ->
            if (intervals.isEmpty()) {
                flowOf(intervals)
            } else {
                val intervalsFlows: List<Flow<Interval>> = intervals.map { it.asFlow() }
                combine(intervalsFlows) { intervals.toCopyableList().copy() }
            }
        }
        .share()

    val isRemoveIntervalButtonEnabled: Flow<Boolean> = intervals.map { it.size > 1 }

    val canBeEdited: Flow<Boolean> = intervals.map { it.isNotEmpty() }

    override fun onCleared() {
        getKoin().getScope(INTERVALS_SCOPE_ID).close()
    }
}