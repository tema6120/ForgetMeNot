package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class SpeakEventDialogState: FlowableState<SpeakEventDialogState>() {
    var dialogPurpose: DialogPurpose? by me<DialogPurpose?>(null)
    var selectedRadioButton: SpeakEvent? by me<SpeakEvent?>(null)
    var delayInput: String by me("2")

    sealed class DialogPurpose {
        object ToAddNewSpeakEvent : DialogPurpose()
        class ToChangeAtPosition(val position: Int) : DialogPurpose()
    }
    enum class SpeakEvent {
        SpeakQuestion,
        SpeakAnswer,
        Delay
    }
}