package com.odnovolov.forgetmenot.screen.decksettings

import com.odnovolov.forgetmenot.common.entity.TestMethod

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
    class TestMethodWasChosen(val testMethod: TestMethod) : DeckSettingsEvent()
    object IntervalsButtonClicked : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object DisplayQuestionSwitchToggled : DeckSettingsEvent()
}