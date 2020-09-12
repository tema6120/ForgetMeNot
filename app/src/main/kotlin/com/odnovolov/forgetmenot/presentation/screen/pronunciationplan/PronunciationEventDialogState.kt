package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import kotlinx.serialization.Serializable

class PronunciationEventDialogState : FlowMaker<PronunciationEventDialogState>() {
    var dialogPurpose: DialogPurpose? by flowMaker<DialogPurpose?>(null)
    var selectedRadioButton: PronunciationEventType? by flowMaker<PronunciationEventType?>(null)
    var delayInput: String by flowMaker("2")
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