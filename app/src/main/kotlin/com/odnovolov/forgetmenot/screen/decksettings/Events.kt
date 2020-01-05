package com.odnovolov.forgetmenot.screen.decksettings

import com.odnovolov.forgetmenot.common.entity.CardReverse
import com.odnovolov.forgetmenot.common.entity.TestMethod

sealed class DeckSettingsEvent {
    object RenameDeckButtonClicked : DeckSettingsEvent()
    class RenameDeckDialogTextChanged(val text: String) : DeckSettingsEvent()
    object RenameDeckDialogPositiveButtonClicked : DeckSettingsEvent()
    object RenameDeckDialogNegativeButtonClicked : DeckSettingsEvent()

    object SaveExercisePreferenceButtonClicked : DeckSettingsEvent()
    class SetExercisePreferenceButtonClicked(val id: Long) : DeckSettingsEvent()
    class RenameExercisePreferenceButtonClicked(val id: Long) : DeckSettingsEvent()
    class DeleteExercisePreferenceButtonClicked(val id: Long) : DeckSettingsEvent()
    object AddNewExercisePreferenceButtonClicked : DeckSettingsEvent()

    class NamePresetDialogTextChanged(val text: String) : DeckSettingsEvent()
    object NamePresetPositiveDialogButtonClicked : DeckSettingsEvent()
    object NamePresetNegativeDialogButtonClicked : DeckSettingsEvent()

    object RandomOrderSwitchToggled : DeckSettingsEvent()
    class TestMethodWasSelected(val testMethod: TestMethod) : DeckSettingsEvent()
    object IntervalsButtonClicked : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object DisplayQuestionSwitchToggled : DeckSettingsEvent()
    class CardReverseWasSelected(val cardReverse: CardReverse) : DeckSettingsEvent()
}