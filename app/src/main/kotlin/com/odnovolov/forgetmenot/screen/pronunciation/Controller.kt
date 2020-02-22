package com.odnovolov.forgetmenot.screen.pronunciation

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.screen.pronunciation.PronunciationEvent.*
import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus.*
import com.odnovolov.forgetmenot.screen.pronunciation.PronunciationOrder.SetDialogText

class PronunciationController : BaseController<PronunciationEvent, PronunciationOrder>() {
    val queries: PronunciationControllerQueries = database.pronunciationControllerQueries

    override fun handleEvent(event: PronunciationEvent) {
        when (event) {
            SavePronunciationButtonClicked -> {
                setNamePresetDialogStatus(VisibleToMakeIndividualPresetAsShared)
            }

            is SetPronunciationButtonClicked -> {
                queries.setCurrentPronunciation(event.pronunciationId)
            }

            is RenamePronunciationButtonClicked -> {
                val name: String? = queries.getPronunciationNameById(event.pronunciationId)
                    .executeAsOneOrNull()
                if (!name.isNullOrEmpty()) {
                    queries.setRenamePronunciationId(event.pronunciationId)
                    setNamePresetDialogStatus(VisibleToRenameSharedPreset)
                    issueOrder(SetDialogText(name))
                }
            }

            is DeletePronunciationButtonClicked -> {
                queries.deleteSharedPronunciation(event.pronunciationId)
            }

            AddNewPronunciationButtonClicked -> {
                setNamePresetDialogStatus(VisibleToCreateNewSharedPreset)
            }

            is DialogTextChanged -> {
                queries.setTypedPronunciationName(event.text)
                checkName()
            }

            PositiveDialogButtonClicked -> {
                if (checkName() === Ok) {
                    when (getNamePresetDialogStatus()) {
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
                    setNamePresetDialogStatus(Invisible)
                }
            }

            NegativeDialogButtonClicked -> {
                setNamePresetDialogStatus(Invisible)
            }

            is AvailableLanguagesUpdated -> {
                val value = event.languages.toList()
                val availableLanguages = listOfLocalesAdapter.encode(value)
                queries.setAvailableLanguages(availableLanguages)
            }

            is QuestionLanguageSelected -> {
                queries.setQuestionLanguage(event.language)
            }

            QuestionAutoSpeakSwitchToggled -> {
                queries.toggleQuestionAutoSpeak()
            }

            is AnswerLanguageSelected -> {
                queries.setAnswerLanguage(event.language)
            }

            AnswerAutoSpeakSwitchToggled -> {
                queries.toggleAnswerAutoSpeak()
            }

            DoNotSpeakTextInBracketsSwitchToggled -> {
                queries.toggleDoNotSpeakTextInBrackets()
            }
        }
    }

    private fun setNamePresetDialogStatus(status: NamePresetDialogStatus) {
        val databaseValue = namePresetDialogStatusAdapter.encode(status)
        queries.setNamePresetDialogStatus(databaseValue)
    }

    private fun getNamePresetDialogStatus(): NamePresetDialogStatus {
        val databaseValue = queries.getNamePresetDialogStatus().executeAsOne()
        return namePresetDialogStatusAdapter.decode(databaseValue)
    }

    private fun checkName(): NameCheckResult {
        val nameCheckResult = when {
            queries.isTypedPronunciationNameEmpty().executeAsOne() -> Empty
            queries.isTypedPronunciationNameOccupied().executeAsOne() -> Occupied
            else -> Ok
        }
        queries.setNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }

}