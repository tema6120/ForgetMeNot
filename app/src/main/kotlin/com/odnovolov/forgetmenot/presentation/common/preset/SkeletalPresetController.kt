package com.odnovolov.forgetmenot.presentation.common.preset

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.common.preset.PresetEvent.*
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController.Command
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController.Command.ShowDialogWithText

abstract class SkeletalPresetController(
    private val dialogState: PresetDialogState,
    private val dialogStateProvider: ShortTermStateProvider<PresetDialogState>,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<PresetEvent, Command>() {
    sealed class Command {
        class ShowDialogWithText(val text: String) : Command()
    }

    override fun handle(event: PresetEvent) {
        when (event) {
            SavePresetButtonClicked -> {
                dialogState.purpose = ToMakeIndividualPresetAsShared
                sendCommand(ShowDialogWithText(""))
            }

            is SetPresetButtonClicked -> {
                onSetPresetButtonClicked(event.id)
            }

            is RenamePresetButtonClicked -> {
                dialogState.purpose = ToRenameSharedPreset(event.id)
                val currentName: String = getPresetName(event.id)
                sendCommand(ShowDialogWithText(currentName))
            }

            is DeletePresetButtonClicked -> {
                onDeletePresetButtonClicked(event.id)
            }

            AddNewPresetButtonClicked -> {
                dialogState.purpose = ToCreateNewSharedPreset
                sendCommand(ShowDialogWithText(""))
            }

            is PresetNameInputChanged -> {
                dialogState.typedPresetName = event.typedName
            }

            PresetNamePositiveDialogButtonClicked -> {
                onPresetNamePositiveDialogButtonClicked()
            }
        }
    }

    protected abstract fun onSetPresetButtonClicked(id: Long?)

    protected abstract fun getPresetName(id: Long): String

    protected abstract fun onDeletePresetButtonClicked(id: Long)

    protected abstract fun onPresetNamePositiveDialogButtonClicked()

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}