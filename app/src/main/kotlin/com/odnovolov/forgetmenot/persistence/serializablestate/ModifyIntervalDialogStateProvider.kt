package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import kotlinx.serialization.Serializable

object ModifyIntervalDialogStateProvider {
    fun load(deckSettingsState: DeckSettings.State): ModifyIntervalDialogState {
        return loadSerializable(SerializableModifyIntervalDialogState.serializer())
            ?.toOriginal(deckSettingsState)
            ?: throw  IllegalStateException("No ModifyIntervalDialogState in the Store")
    }

    fun save(state: ModifyIntervalDialogState) {
        val serializable: SerializableModifyIntervalDialogState = state.toSerializable()
        saveSerializable(serializable, SerializableModifyIntervalDialogState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableModifyIntervalDialogState::class)
    }

    @Serializable
    private data class SerializableModifyIntervalDialogState(
        val intervalId: Long,
        val intervalInputValue: Int?,
        val intervalUnit: IntervalUnit
    )

    private fun ModifyIntervalDialogState.toSerializable() = SerializableModifyIntervalDialogState(
        intervalId = interval.id,
        intervalInputValue = displayedInterval.value,
        intervalUnit = displayedInterval.intervalUnit
    )

    private fun SerializableModifyIntervalDialogState.toOriginal(
        deckSettingsState: DeckSettings.State
    ): ModifyIntervalDialogState {
        val interval: Interval = deckSettingsState.deck.exercisePreference.intervalScheme!!
            .intervals.find { it.id == this.intervalId }!!
        val intervalInputData =
            DisplayedInterval(
                intervalInputValue,
                intervalUnit
            )
        return ModifyIntervalDialogState(interval, intervalInputData)
    }
}