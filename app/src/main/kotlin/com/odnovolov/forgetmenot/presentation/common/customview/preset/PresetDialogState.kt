package com.odnovolov.forgetmenot.presentation.common.customview.preset

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import kotlinx.serialization.Serializable

class PresetDialogState : FlowMaker<PresetDialogState>() {
    var purpose: DialogPurpose? by flowMaker<DialogPurpose?>(null)
    var typedPresetName: String by flowMaker("")
    var idToDelete: Long? by flowMaker<Long?>(null)
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