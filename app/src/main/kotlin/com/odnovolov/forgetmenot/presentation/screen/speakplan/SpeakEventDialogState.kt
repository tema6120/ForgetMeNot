package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import kotlinx.serialization.Serializable

class SpeakEventDialogState : FlowableState<SpeakEventDialogState>() {
    var dialogPurpose: DialogPurpose? by me<DialogPurpose?>(null)
    var selectedRadioButton: SpeakEventType? by me<SpeakEventType?>(null)
    var delayInput: String by me("2")
}

@Serializable
sealed class DialogPurpose {
    @Serializable
    object ToAddNewSpeakEvent : DialogPurpose()
    @Serializable
    class ToChangeSpeakEventAtPosition(val position: Int) : DialogPurpose()
}

enum class SpeakEventType {
    SpeakQuestion,
    SpeakAnswer,
    Delay
}