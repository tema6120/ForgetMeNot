package com.odnovolov.forgetmenot.presentation.common.preset

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController.Command.ShowDialogWithText
import kotlinx.coroutines.flow.Flow

abstract class SkeletalPresetController(
    private val dialogState: PresetDialogState,
    private val dialogStateProvider: ShortTermStateProvider<PresetDialogState>
) {
    sealed class Command {
        class ShowDialogWithText(val text: String) : Command()
    }

    protected val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()

    fun onSavePresetButtonClicked() {
        dialogState.purpose = ToMakeIndividualPresetAsShared
        commandFlow.send(ShowDialogWithText(""))
    }

    abstract fun onSetPresetButtonClicked(id: Long?)

    fun onRenamePresetButtonClicked(id: Long) {
        dialogState.purpose = ToRenameSharedPreset(id)
        val currentName: String = getPresetName(id)
        commandFlow.send(ShowDialogWithText(currentName))
    }

    protected abstract fun getPresetName(id: Long): String

    abstract fun onDeletePresetButtonClicked(id: Long)

    fun onAddNewPresetButtonClicked() {
        dialogState.purpose = ToCreateNewSharedPreset
        commandFlow.send(ShowDialogWithText(""))
    }

    fun onPresetNameInputChanged(typedName: String) {
        dialogState.typedPresetName = typedName
    }

    abstract fun onPresetNamePositiveDialogButtonClicked()

    fun onFragmentPause() {
        dialogStateProvider.save(dialogState)
    }
}