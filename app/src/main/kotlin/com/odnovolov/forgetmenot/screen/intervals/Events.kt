package com.odnovolov.forgetmenot.screen.intervals

sealed class IntervalsEvent {
    object SaveIntervalSchemeButtonClicked : IntervalsEvent()
    class SetIntervalSchemeButtonClicked(val intervalSchemeId: Long?) : IntervalsEvent()
    class RenameIntervalSchemeButtonClicked(val intervalSchemeId: Long) : IntervalsEvent()
    class DeleteIntervalSchemeButtonClicked(val intervalSchemeId: Long) : IntervalsEvent()
    object AddNewIntervalSchemeButtonClicked : IntervalsEvent()
    class DialogTextChanged(val text: String) : IntervalsEvent()
    object PositiveDialogButtonClicked : IntervalsEvent()
    object NegativeDialogButtonClicked : IntervalsEvent()
    class ModifyIntervalButtonClicked(val targetLevelOfKnowledge: Int) : IntervalsEvent()
    object AddIntervalButtonClicked : IntervalsEvent()
    object RemoveIntervalButtonClicked : IntervalsEvent()
}