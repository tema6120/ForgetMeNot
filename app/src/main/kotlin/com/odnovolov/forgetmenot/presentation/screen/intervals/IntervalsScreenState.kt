package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class IntervalsScreenState : FlowableState<IntervalsScreenState>() {
    var namePresetDialogStatus: NamePresetDialogStatus by me(NamePresetDialogStatus.Invisible)
    var typedPresetName: String by me("")
    var renamePresetId: Long? by me(null)
}