package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import kotlinx.coroutines.flow.*

class IntervalsViewModel(
    deckSettingsState: DeckSettings.State,
    screenState: IntervalsScreenState
) {
    @OptIn(ExperimentalStdlibApi::class)
    val intervalItems: Flow<List<IntervalListItem>> =
        deckSettingsState.deck.flowOf(Deck::exercisePreference)
            .flatMapLatest { exercisePreference: ExercisePreference ->
                exercisePreference.flowOf(ExercisePreference::intervalScheme)
            }
            .flatMapLatest { intervalScheme: IntervalScheme? ->
                intervalScheme?.flowOf(IntervalScheme::intervals) ?: flowOf(null)
            }
            .flatMapLatest { intervals: List<Interval>? ->
                if (intervals == null) {
                    screenState.flowOf(IntervalsScreenState::tip)
                        .map { tip: Tip? ->
                            val header = IntervalListItem.Header(tip, areIntervalsOn = false)
                            listOf(header)
                        }
                } else {
                    val intervalsFlows: List<Flow<Interval>> = intervals.map { it.asFlow() }
                    combine(intervalsFlows) { intervals.toCopyableList().copy() }
                        .combine(screenState.flowOf(IntervalsScreenState::tip)) { intervals: List<Interval>,
                                                                                  tip: Tip? ->
                            val intervalWrappers = intervals.map { interval: Interval ->
                                IntervalListItem.IntervalWrapper(interval)
                            }

                            val maxGrade: Int = intervals.map { it.grade }.maxOrNull()!!
                            val excellentGrade: Int = maxGrade + 1
                            buildList {
                                add(IntervalListItem.Header(tip, areIntervalsOn = true))
                                addAll(intervalWrappers)
                                add(IntervalListItem.Footer(excellentGrade))
                            }
                        }
                }
            }
}