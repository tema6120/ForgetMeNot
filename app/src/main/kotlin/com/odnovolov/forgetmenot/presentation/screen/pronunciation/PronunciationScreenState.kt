package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class PronunciationScreenState : FlowableState<PronunciationScreenState>() {
    var namePresetDialogStatus: NamePresetDialogStatus by me(NamePresetDialogStatus.Invisible)
    var typedPresetName: String by me("")
    var renamePresetId: Long? by me(null)
}