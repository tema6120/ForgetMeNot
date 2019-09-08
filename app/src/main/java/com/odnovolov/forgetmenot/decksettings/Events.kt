package com.odnovolov.forgetmenot.decksettings

sealed class DeckSettingsEvent {
    object RenameDeckButtonClicked : DeckSettingsEvent()
    object SaveExercisePreferenceButtonClicked : DeckSettingsEvent()
    class SetExercisePreferenceButtonClicked(val id: Long) : DeckSettingsEvent()
    class RenameExercisePreferenceButtonClicked(val id: Long) : DeckSettingsEvent()
    class DeleteExercisePreferenceButtonClicked(val id: Long) : DeckSettingsEvent()
    object AddNewExercisePreferenceButtonClicked : DeckSettingsEvent()
    class DialogTextChanged(val text: String) : DeckSettingsEvent()
    object PositiveDialogButtonClicked : DeckSettingsEvent()
    object NegativeDialogButtonClicked : DeckSettingsEvent()
    object RandomOrderSwitchToggled : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
}