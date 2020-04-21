package com.odnovolov.forgetmenot.presentation.common.customview.preset

sealed class PresetEvent {
    object SavePresetButtonClicked : PresetEvent()
    class SetPresetButtonClicked(val id: Long?) : PresetEvent()
    class RenamePresetButtonClicked(val id: Long) : PresetEvent()
    class DeletePresetButtonClicked(val id: Long) : PresetEvent()
    object AddNewPresetButtonClicked : PresetEvent()
    class PresetNameInputChanged(val typedName: String) : PresetEvent()
    object PresetNamePositiveDialogButtonClicked : PresetEvent()
}