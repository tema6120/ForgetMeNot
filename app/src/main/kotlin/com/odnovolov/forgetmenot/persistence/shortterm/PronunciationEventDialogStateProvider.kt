package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationEventDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.DialogPurpose
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationEventType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PronunciationEventDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = PronunciationEventDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<PronunciationEventDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val dialogPurpose: DialogPurpose?,
        val selectedRadioButton: PronunciationEventType?,
        val delayInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PronunciationEventDialogState) = SerializableState(
        state.dialogPurpose,
        state.selectedRadioButton,
        state.delayInput
    )

    override fun toOriginal(serializableState: SerializableState) =
        PronunciationEventDialogState().apply {
            dialogPurpose = serializableState.dialogPurpose
            selectedRadioButton = serializableState.selectedRadioButton
            delayInput = serializableState.delayInput
        }
}