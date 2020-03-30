package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus

class RepetitionSettingsScreenState : FlowableState<RepetitionSettingsScreenState>() {
    var namePresetDialogStatus: NamePresetDialogStatus by me(NamePresetDialogStatus.Invisible)
    var typedPresetName: String by me("")
    var renamePresetId: Long? by me(null)
}