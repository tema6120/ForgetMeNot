package com.odnovolov.forgetmenot.screen.pronunciation

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.entity.PresetNameInputDialogStatus
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.screen.pronunciation.PronunciationEvent.*
import com.odnovolov.forgetmenot.common.entity.PresetNameInputDialogStatus.*
import com.odnovolov.forgetmenot.pronunciation.PronunciationControllerQueries
import com.odnovolov.forgetmenot.screen.pronunciation.PronunciationOrder.SetDialogText

class PronunciationController : BaseController<PronunciationEvent, PronunciationOrder>() {
    val queries: PronunciationControllerQueries = database.pronunciationControllerQueries

    override fun handleEvent(event: PronunciationEvent) {
        when (event) {
            SavePronunciationButtonClicked -> {
                setPresetNameInputDialogStatus(VisibleToMakeIndividualPresetAsShared)
            }

            is SetPronunciationButtonClicked -> {
                queries.setCurrentPronunciation(event.pronunciationId)
            }

            is RenamePronunciationButtonClicked -> {
                val name = queries.getPronunciationNameById(event.pronunciationId)
                    .executeAsOneOrNull()
                if (!name.isNullOrEmpty()) {
                    queries.setRenamePronunciationId(event.pronunciationId)
                    setPresetNameInputDialogStatus(VisibleToRenameSharedPreset)
                    issueOrder(SetDialogText(name))
                }
            }

            is DeletePronunciationButtonClicked -> {
                queries.deleteSharedPronunciation(event.pronunciationId)
            }

            AddNewPronunciationButtonClicked -> {
                setPresetNameInputDialogStatus(VisibleToCreateNewSharedPreset)
            }

            is DialogTextChanged -> {
                queries.setTypedPronunciationName(event.text)
                checkName()
            }

            PositiveDialogButtonClicked -> {
                if (checkName() === OK) {
                    when (getNameInputDialogStatus()) {
                        VisibleToMakeIndividualPresetAsShared -> {
                            queries.renameCurrent()
                        }
                        VisibleToCreateNewSharedPreset -> {
                            queries.createNewShared()
                            queries.bindNewPronunciationToCurrentExercisePreference()
                        }
                        VisibleToRenameSharedPreset -> {
                            queries.renameShared()
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

            is AvailableLanguagesUpdated -> {
                val availableLanguages = listOfLocalesAdapter.encode(event.languages.toList())
                queries.setAvailableLanguages(availableLanguages)
            }

            is QuestionLanguageSelected -> {
                queries.setQuestionLanguage(event.language)
            }

            is QuestionAutoSpeakSwitchToggled -> {
                queries.setQuestionAutoSpeak(event.isOn)
            }

            is AnswerLanguageSelected -> {
                queries.setAnswerLanguage(event.language)
            }

            is AnswerAutoSpeakSwitchToggled -> {
                queries.setAnswerAutoSpeak(event.isOn)
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
            queries.isTypedPronunciationNameEmpty().executeAsOne().asBoolean() -> EMPTY
            queries.isTypedPronunciationNameOccupied().executeAsOne().asBoolean() -> OCCUPIED
            else -> OK
        }
        queries.setNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }

}