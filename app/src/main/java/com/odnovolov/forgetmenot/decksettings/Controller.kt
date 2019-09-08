package com.odnovolov.forgetmenot.decksettings

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.NameCheckResult
import com.odnovolov.forgetmenot.common.NameCheckResult.*
import com.odnovolov.forgetmenot.common.PresetNameInputDialogStatus
import com.odnovolov.forgetmenot.common.PresetNameInputDialogStatus.*
import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.nameCheckResultAdapter
import com.odnovolov.forgetmenot.common.database.presetNameInputDialogStatusAdapter
import com.odnovolov.forgetmenot.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.decksettings.DeckSettingsOrder.*

class DeckSettingsController : BaseController<DeckSettingsEvent, DeckSettingsOrder>() {
    private val queries: DeckSettingsControllerQueries = database.deckSettingsControllerQueries

    override fun handleEvent(event: DeckSettingsEvent) {
        when (event) {
            RenameDeckButtonClicked -> {
                issueOrder(ShowRenameDeckDialog)
            }

            SaveExercisePreferenceButtonClicked -> {
                setPresetNameInputDialogStatus(VisibleToMakeIndividualPresetAsShared)
            }

            is SetExercisePreferenceButtonClicked -> {
                queries.setCurrentExercisePreference(event.id)
            }

            is RenameExercisePreferenceButtonClicked -> {
                val name = queries.getExercisePreferenceNameById(event.id).executeAsOneOrNull()
                if (!name.isNullOrEmpty()) {
                    queries.setRenamePresetId(event.id)
                    setPresetNameInputDialogStatus(VisibleToRenameSharedPreset)
                    issueOrder(SetDialogText(name))
                }
            }

            is DeleteExercisePreferenceButtonClicked -> {
                //TODO
            }

            AddNewExercisePreferenceButtonClicked -> {
                //TODO
            }

            is DialogTextChanged -> {
                queries.setTypedPresetName(event.text)
                checkName()
            }

            PositiveDialogButtonClicked -> {
                if (checkName() === OK) {
                    when (getNameInputDialogStatus()) {
                        VisibleToMakeIndividualPresetAsShared -> {
                            queries.renameCurrentPreset()
                        }
                        VisibleToCreateNewSharedPreset -> {

                        }
                        VisibleToRenameSharedPreset -> {
                            queries.renameSharedPreset()
                        }
                        else -> {
                        }
                    }
                    setPresetNameInputDialogStatus(Invisible)
                }
            }

            NegativeDialogButtonClicked -> {
                setPresetNameInputDialogStatus(Invisible)
            }

            RandomOrderSwitchToggled -> {
                queries.toggleRandomOrder()
            }

            PronunciationButtonClicked -> {
                with(database.pronunciationInitQueries) {
                    dropTablePronunciationState()
                    createTablePronunciationState()
                    initPronunciationState()
                    createTriggerPreventRemovalOfDefaultPronunciation()
                    createTriggerOnTryToModifyDefaultPronunciation()
                    createTriggerSetDefaultPronunciationIfNeed()
                    createTriggerOnDeletePronunciation()
                    createTriggerDeleteUnusedIndividualPronunciation()
                }
                issueOrder(NavigateToPronunciation)
            }
        }
    }

    private fun setPresetNameInputDialogStatus(status: PresetNameInputDialogStatus) {
        val databaseValue = presetNameInputDialogStatusAdapter.encode(status)
        queries.setPresetNameInputDialogStatus(databaseValue)
    }

    private fun getNameInputDialogStatus(): PresetNameInputDialogStatus {
        val databaseValue = queries.getPresetNameInputDialogStatus().executeAsOne()
        return presetNameInputDialogStatusAdapter.decode(databaseValue)
    }

    private fun checkName(): NameCheckResult {
        val nameCheckResult = when {
            queries.isTypedPresetNameEmpty().executeAsOne().asBoolean() -> EMPTY
            queries.isTypedPresetNameOccupied().executeAsOne().asBoolean() -> OCCUPIED
            else -> OK
        }
        queries.setNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }
}