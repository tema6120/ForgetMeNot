package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset.DialogPurpose.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset.PresetEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset.SkeletalPresetController.Command
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset.SkeletalPresetController.Command.ShowPresetNameDialog

abstract class SkeletalPresetController(
    private val dialogState: PresetDialogState,
    private val dialogStateProvider: ShortTermStateProvider<PresetDialogState>,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<PresetEvent, Command>() {
    sealed class Command {
        class ShowPresetNameDialog(val presetName: String) : Command()
        object ShowRemovePresetDialog : Command()
    }

    override fun handle(event: PresetEvent) {
        when (event) {
            SavePresetButtonClicked -> {
                dialogState.purpose = ToMakeIndividualPresetAsShared
                sendCommand(ShowPresetNameDialog(""))
            }

            is SetPresetButtonClicked -> {
                onSetPresetButtonClicked(event.id)
            }

            is RenamePresetButtonClicked -> {
                dialogState.purpose = ToRenameSharedPreset(event.id)
                val currentName: String = getPresetName(event.id)
                sendCommand(ShowPresetNameDialog(currentName))
            }

            is DeletePresetButtonClicked -> {
                onDeletePresetButtonClicked(event.id)
            }

            HelpButtonClicked -> {
                onHelpButtonClicked()
            }

            AddNewPresetButtonClicked -> {
                dialogState.purpose = ToCreateNewSharedPreset
                sendCommand(ShowPresetNameDialog(""))
            }

            is PresetNameInputChanged -> {
                dialogState.typedPresetName = event.typedName
            }

            PresetNamePositiveDialogButtonClicked -> {
                onPresetNamePositiveDialogButtonClicked()
            }

            RemovePresetPositiveDialogButtonClicked -> {
                onRemovePresetPositiveDialogButtonClicked()
            }
        }
    }

    protected abstract fun onSetPresetButtonClicked(id: Long?)

    protected abstract fun getPresetName(id: Long): String

    protected abstract fun onDeletePresetButtonClicked(id: Long)

    protected abstract fun onPresetNamePositiveDialogButtonClicked()

    protected abstract fun onRemovePresetPositiveDialogButtonClicked()

    protected abstract fun onHelpButtonClicked()

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}