package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.SpeakEventDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.speakplan.DialogPurpose
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SpeakEventDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = SpeakEventDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<SpeakEventDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val dialogPurpose: DialogPurpose?,
        val selectedRadioButton: SpeakEventType?,
        val delayInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: SpeakEventDialogState) = SerializableState(
        state.dialogPurpose,
        state.selectedRadioButton,
        state.delayInput
    )

    override fun toOriginal(serializableState: SerializableState) = SpeakEventDialogState().apply {
        dialogPurpose = serializableState.dialogPurpose
        selectedRadioButton = serializableState.selectedRadioButton
        delayInput = serializableState.delayInput
    }
}