package com.odnovolov.forgetmenot.presentation.common.customview.preset

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import kotlinx.serialization.Serializable

class PresetDialogState : FlowableState<PresetDialogState>() {
    var purpose: DialogPurpose? by me<DialogPurpose?>(null)
    var typedPresetName: String by me("")
}

@Serializable
sealed class DialogPurpose {
    @Serializable
    object ToMakeIndividualPresetAsShared : DialogPurpose()
    @Serializable
    object ToCreateNewSharedPreset : DialogPurpose()
    @Serializable
    class ToRenameSharedPreset(val id: Long) : DialogPurpose()
}