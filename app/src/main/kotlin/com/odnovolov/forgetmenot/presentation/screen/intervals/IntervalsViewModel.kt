package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.*

class IntervalsViewModel(
    deckSettingsState: DeckSettings.State
) {
    @OptIn(ExperimentalStdlibApi::class)
    val intervals: Flow<List<IntervalListItem>> =
        deckSettingsState.deck.flowOf(Deck::exercisePreference)
            .flatMapLatest { exercisePreference: ExercisePreference ->
                exercisePreference.flowOf(ExercisePreference::intervalScheme)
            }
            .flatMapLatest { intervalScheme: IntervalScheme? ->
                intervalScheme?.flowOf(IntervalScheme::intervals) ?: flowOf(null)
            }
            .flatMapLatest { intervals: List<Interval>? ->
                if (intervals == null) {
                    flowOf(listOf(IntervalListItem.Header(areIntervalsOn = false)))
                } else {
                    val intervalsFlows: List<Flow<Interval>> = intervals.map { it.asFlow() }
                    combine(intervalsFlows) { intervals.toCopyableList().copy() }
                        .map { intervals: List<Interval> ->
                            val intervalWrappers = intervals.map { interval: Interval ->
                                IntervalListItem.IntervalWrapper(interval)
                            }

                            val maxGrade: Int = intervals.map { it.grade }.maxOrNull()!!
                            val excellentGrade: Int = maxGrade + 1
                            buildList {
                                add(IntervalListItem.Header(areIntervalsOn = true))
                                addAll(intervalWrappers)
                                add(IntervalListItem.Footer(excellentGrade))
                            }
                        }
                }
            }
}