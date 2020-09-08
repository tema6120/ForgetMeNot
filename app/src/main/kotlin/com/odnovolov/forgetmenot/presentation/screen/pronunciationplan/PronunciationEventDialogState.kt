package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import kotlinx.serialization.Serializable

class PronunciationEventDialogState : FlowableState<PronunciationEventDialogState>() {
    var dialogPurpose: DialogPurpose? by me<DialogPurpose?>(null)
    var selectedRadioButton: PronunciationEventType? by me<PronunciationEventType?>(null)
    var delayInput: String by me("2")
}

@Serializable
sealed class DialogPurpose {
    @Serializable
    object ToAddNewPronunciationEvent : DialogPurpose()

    @Serializable
    class ToChangePronunciationEventAtPosition(val position: Int) : DialogPurpose()
}

enum class PronunciationEventType {
    SpeakQuestion,
    SpeakAnswer,
    Delay
}