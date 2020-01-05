package com.odnovolov.forgetmenot.screen.decksettings

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus.*
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.nameCheckResultAdapter
import com.odnovolov.forgetmenot.common.database.namePresetDialogStatusAdapter
import com.odnovolov.forgetmenot.screen.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.screen.decksettings.DeckSettingsOrder.*

class DeckSettingsController : BaseController<DeckSettingsEvent, DeckSettingsOrder>() {
    private val queries: DeckSettingsControllerQueries = database.deckSettingsControllerQueries

    override fun handleEvent(event: DeckSettingsEvent) {
        when (event) {
            RenameDeckButtonClicked -> {
                queries.setIsRenameDeckDialogVisible(true)
                val deckName: String = queries.getDeckName().executeAsOne()
                issueOrder(SetRenameDeckDialogText(deckName))
            }

            is RenameDeckDialogTextChanged -> {
                queries.setTypedDeckName(event.text)
                checkTypedDeckName()
            }

            RenameDeckDialogPositiveButtonClicked -> {
                if (checkTypedDeckName() == OK) {
                    queries.renameDeck()
                    queries.resetRenameDeckState()
                }
            }

            RenameDeckDialogNegativeButtonClicked -> {
                queries.resetRenameDeckState()
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
                    issueOrder(SetNamePresetDialogText(name))
                }
            }

            is DeleteExercisePreferenceButtonClicked -> {
                queries.deleteSharedExercisePreference(event.id)
            }

            AddNewExercisePreferenceButtonClicked -> {
                setPresetNameInputDialogStatus(VisibleToCreateNewSharedPreset)
            }

            is NamePresetDialogTextChanged -> {
                queries.setTypedPresetName(event.text)
                checkTypedPresetName()
            }

            NamePresetPositiveDialogButtonClicked -> {
                if (checkTypedPresetName() === OK) {
                    when (getNameInputDialogStatus()) {
                        VisibleToMakeIndividualPresetAsShared -> {
                            queries.renameCurrentPreset()
                        }
                        VisibleToCreateNewSharedPreset -> {
                            queries.createNewSharedExercisePreference()
                            queries.bindNewExercisePreferenceToDeck()
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

            NamePresetNegativeDialogButtonClicked -> {
                setPresetNameInputDialogStatus(Invisible)
            }

            RandomOrderSwitchToggled -> {
                queries.toggleRandomOrder()
            }

            is TestMethodWasSelected -> {
                queries.setTestMethod(event.testMethod)
            }

            IntervalsButtonClicked -> {
                queries.cleanIntervalsState()
                queries.initIntervalsState()
                issueOrder(NavigateToIntervals)
            }

            PronunciationButtonClicked -> {
                queries.cleanPronunciationState()
                queries.initPronunciationState()
                issueOrder(NavigateToPronunciation)
            }

            DisplayQuestionSwitchToggled -> {
                queries.toggleIsQuestionDisplayed()
            }

            is CardReverseWasSelected -> {
                queries.setCardReverse(event.cardReverse)
            }
        }
    }

    private fun checkTypedDeckName(): NameCheckResult {
        val nameCheckResult = when {
            queries.isTypedDeckNameEmpty().executeAsOne() -> EMPTY
            queries.isTypedDeckNameOccupied().executeAsOne() -> OCCUPIED
            else -> OK
        }
        queries.setDeckNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }

    private fun setPresetNameInputDialogStatus(status: NamePresetDialogStatus) {
        val databaseValue = namePresetDialogStatusAdapter.encode(status)
        queries.setNamePresetDialogStatus(databaseValue)
    }

    private fun getNameInputDialogStatus(): NamePresetDialogStatus {
        val databaseValue = queries.getNamePresetDialogStatus().executeAsOne()
        return namePresetDialogStatusAdapter.decode(databaseValue)
    }

    private fun checkTypedPresetName(): NameCheckResult {
        val nameCheckResult = when {
            queries.isTypedPresetNameEmpty().executeAsOne() -> EMPTY
            queries.isTypedPresetNameOccupied().executeAsOne() -> OCCUPIED
            else -> OK
        }
        queries.setPresetNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }
}