package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.checkIntervalSchemeName
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings.Event.DeniedIntervalSchemeCreation
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings.Event.DeniedIntervalSchemeRenaming
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow

class IntervalsSettings(
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState
) {
    sealed class Event {
        class DeniedIntervalSchemeCreation(val nameCheckResult: NameCheckResult) : Event()
        class DeniedIntervalSchemeRenaming(val nameCheckResult: NameCheckResult) : Event()
    }

    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()
    
    private val exercisePreference: ExercisePreference
        get() = deckSettings.state.deck.exercisePreference
    
    fun setIntervalScheme(intervalSchemeId: Long?) {
        when (intervalSchemeId) {
            deckSettings.state.deck.exercisePreference.intervalScheme?.id -> return
            null -> deckSettings.setIntervalScheme(null)
            IntervalScheme.Default.id -> deckSettings.setIntervalScheme(IntervalScheme.Default)
            else -> {
                globalState.sharedIntervalSchemes
                    .find { it.id == intervalSchemeId }
                    ?.let(deckSettings::setIntervalScheme)
            }
        }
    }
    
    fun createNewSharedIntervalScheme(name: String) {
        when (checkIntervalSchemeName(name, globalState)) {
            Ok -> createNewSharedIntervalSchemeAndSetToCurrentExercisePreference(name)
            Empty -> eventFlow.send(DeniedIntervalSchemeCreation(Empty))
            Occupied -> eventFlow.send(DeniedIntervalSchemeCreation(Occupied))
        }
    }

    private fun createNewSharedIntervalSchemeAndSetToCurrentExercisePreference(name: String) {
        val newSharedIntervalScheme = IntervalScheme(
            id = generateId(),
            name = name,
            intervals = createIntervalsFromDefaultIntervals()
        )
        addNewSharedIntervalScheme(newSharedIntervalScheme)
        deckSettings.setIntervalScheme(newSharedIntervalScheme)
    }

    fun renameIntervalScheme(intervalsScheme: IntervalScheme, newName: String) {
        when (checkIntervalSchemeName(newName, globalState)) {
            Ok -> {
                when {
                    intervalsScheme.isDefault() -> {
                        createNewSharedIntervalSchemeAndSetToCurrentExercisePreference(newName)
                    }
                    intervalsScheme.isIndividual() -> {
                        intervalsScheme.name = newName
                        addNewSharedIntervalScheme(intervalsScheme)
                    }
                    else -> { // current IntervalScheme is shared
                        intervalsScheme.name = newName
                    }
                }
            }
            Empty -> eventFlow.send(DeniedIntervalSchemeRenaming(Empty))
            Occupied -> eventFlow.send(DeniedIntervalSchemeRenaming(Occupied))
        }
    }

    fun deleteSharedIntervalScheme(intervalSchemeId: Long) {
        if (intervalSchemeId == IntervalScheme.Default.id) return
        globalState.sharedIntervalSchemes = globalState.sharedIntervalSchemes
            .filter { sharedIntervalScheme -> sharedIntervalScheme.id != intervalSchemeId }
            .toCopyableList()
        globalState.decks
            .map { deck -> deck.exercisePreference }
            .filter { exercisePreference ->
                exercisePreference.intervalScheme?.let { it.id == intervalSchemeId } ?: false
            }
            .distinct()
            .forEach { exercisePreference ->
                exercisePreference.intervalScheme = IntervalScheme.Default
            }
        deckSettings.recheckIndividualExercisePreferences()
    }

    private fun addNewSharedIntervalScheme(intervalsScheme: IntervalScheme) {
        globalState.sharedIntervalSchemes =
            (globalState.sharedIntervalSchemes + intervalsScheme).toCopyableList()
    }

    fun modifyInterval(targetLevelOfKnowledge: Int, newValue: DateTimeSpan) {
        val isValueChanged: Boolean =
            exercisePreference.intervalScheme?.let { intervalScheme: IntervalScheme ->
                val oldValue = intervalScheme.intervals
                    .find { it.targetLevelOfKnowledge == targetLevelOfKnowledge }
                    ?.value ?: false
                oldValue != newValue
            } ?: false
        if (!isValueChanged) return
        updateIntervalScheme(
            createNewIndividualIntervalScheme = {
                val newIntervals: CopyableList<Interval> = IntervalScheme.Default.intervals
                    .map { defaultInterval: Interval ->
                        val value: DateTimeSpan =
                            if (defaultInterval.targetLevelOfKnowledge == targetLevelOfKnowledge)
                                newValue
                            else
                                defaultInterval.value
                        Interval(
                            id = generateId(),
                            targetLevelOfKnowledge = defaultInterval.targetLevelOfKnowledge,
                            value = value
                        )
                    }
                    .toCopyableList()
                IntervalScheme(id = generateId(), name = "", intervals = newIntervals)
            },
            updateCurrentIntervalScheme = {
                exercisePreference.intervalScheme?.let { intervalScheme: IntervalScheme ->
                    intervalScheme.intervals
                        .find { it.targetLevelOfKnowledge == targetLevelOfKnowledge }
                        ?.value = newValue
                }
            }
        )
    }

    fun addInterval(value: DateTimeSpan) {
        if (exercisePreference.intervalScheme == null) return
        fun newInterval() = Interval(
            id = generateId(),
            targetLevelOfKnowledge = exercisePreference.intervalScheme!!.intervals.last()
                .targetLevelOfKnowledge + 1,
            value = value
        )
        updateIntervalScheme(
            createNewIndividualIntervalScheme = {
                val newIntervals: CopyableList<Interval> =
                    (createIntervalsFromDefaultIntervals() + newInterval())
                        .toCopyableList()
                IntervalScheme(id = generateId(), name = "", intervals = newIntervals)
            },
            updateCurrentIntervalScheme = {
                exercisePreference.intervalScheme?.let { intervalScheme: IntervalScheme ->
                    intervalScheme.intervals = (intervalScheme.intervals + newInterval())
                        .toCopyableList()
                }
            }
        )
    }

    fun removeLastInterval() {
        val hasAtLeastTwoIntervals: Boolean = exercisePreference.intervalScheme
            ?.let { it.intervals.size >= 2 } ?: false
        if (!hasAtLeastTwoIntervals) return
        updateIntervalScheme(
            createNewIndividualIntervalScheme = {
                val newIntervals: CopyableList<Interval> = createIntervalsFromDefaultIntervals()
                    .dropLast(1)
                    .toCopyableList()
                IntervalScheme(id = generateId(), name = "", intervals = newIntervals)
            },
            updateCurrentIntervalScheme = {
                exercisePreference.intervalScheme?.let { intervalScheme: IntervalScheme ->
                    intervalScheme.intervals = intervalScheme.intervals.dropLast(1).toCopyableList()
                }
            }
        )
    }

    private fun createIntervalsFromDefaultIntervals(): CopyableList<Interval> {
        return IntervalScheme.Default.intervals
            .map { defaultInterval: Interval ->
                Interval(
                    id = generateId(),
                    targetLevelOfKnowledge = defaultInterval.targetLevelOfKnowledge,
                    value = defaultInterval.value
                )
            }
            .toCopyableList()
    }

    private inline fun updateIntervalScheme(
        createNewIndividualIntervalScheme: () -> IntervalScheme,
        updateCurrentIntervalScheme: () -> Unit
    ) {
        exercisePreference.intervalScheme?.let { oldIntervalScheme: IntervalScheme ->
            when {
                oldIntervalScheme.isDefault() -> {
                    val newIndividualIntervalScheme = createNewIndividualIntervalScheme()
                    deckSettings.setIntervalScheme(newIndividualIntervalScheme)
                }
                oldIntervalScheme.isIndividual() -> {
                    updateCurrentIntervalScheme()
                    if (oldIntervalScheme.shouldBeDefault()) {
                        deckSettings.setIntervalScheme(IntervalScheme.Default)
                    }
                }
                else -> { // current IntervalScheme is shared
                    updateCurrentIntervalScheme()
                }
            }
        }
    }

    private fun IntervalScheme.shouldBeDefault(): Boolean {
        if (this.name != IntervalScheme.Default.name) return false
        if (this.intervals.size != IntervalScheme.Default.intervals.size) return false
        repeat(this.intervals.size) { index ->
            val thisInterval = this.intervals[index]
            val defaultInterval = IntervalScheme.Default.intervals[index]
            if (thisInterval.targetLevelOfKnowledge != defaultInterval.targetLevelOfKnowledge
                || thisInterval.value != defaultInterval.value
            ) {
                return false
            }
        }
        return true
    }
}