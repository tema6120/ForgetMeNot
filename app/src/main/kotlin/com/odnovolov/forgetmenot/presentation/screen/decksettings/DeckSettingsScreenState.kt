package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class DeckSettingsScreenState : FlowableState<DeckSettingsScreenState>() {
    var isRenameDeckDialogVisible: Boolean by me(false)
    var typedDeckName: String by me("")
    var namePresetDialogStatus: NamePresetDialogStatus by me(NamePresetDialogStatus.Invisible)
    var typedPresetName: String by me("")
    var renamePresetId: Long? by me(null)
}