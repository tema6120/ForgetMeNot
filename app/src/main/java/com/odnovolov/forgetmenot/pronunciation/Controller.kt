package com.odnovolov.forgetmenot.pronunciation

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.NameCheckResult
import com.odnovolov.forgetmenot.common.NameCheckResult.*
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.pronunciation.PronunciationEvent.*
import com.odnovolov.forgetmenot.pronunciation.NameInputDialogStatus.*
import com.odnovolov.forgetmenot.pronunciation.PronunciationOrder.SetDialogText

class PronunciationController : BaseController<PronunciationEvent, PronunciationOrder>() {
    val queries: PronunciationControllerQueries = database.pronunciationControllerQueries

    override fun handleEvent(event: PronunciationEvent) {
        when (event) {
            SavePronunciationButtonClicked -> {
                setNameInputDialogStatus(VisibleToMakeIndividualPronunciationShared)
            }

            is SetPronunciationButtonClicked -> {
                queries.setPronunciationId(event.pronunciationId)
            }

            is RenamePronunciationButtonClicked -> {
                val name = queries.getPronunciationNameById(event.pronunciationId)
                    .executeAsOneOrNull()
                if (!name.isNullOrEmpty()) {
                    queries.setRenamePronunciationId(event.pronunciationId)
                    setNameInputDialogStatus(VisibleToRenameSharedPronunciation)
                    issueOrder(SetDialogText(name))
                }
            }

            is DeletePronunciationButtonClicked -> {
                queries.delete(event.pronunciationId)
            }

            AddNewPronunciationButtonClicked -> {
                setNameInputDialogStatus(VisibleToCreateNewSharedPronunciation)
            }

            is DialogTextChanged -> {
                queries.setTypedPronunciationName(event.text)
                checkName()
            }

            PositiveDialogButtonClicked -> {
                if (checkName() === OK) {
                    when (getNameInputDialogStatus()) {
                        VisibleToMakeIndividualPronunciationShared -> {
                            queries.renameCurrent()
                        }
                        VisibleToCreateNewSharedPronunciation -> {
                            queries.createNewShared()
                            queries.bindNewPronunciationToCurrentExercisePreference()
                        }
                        VisibleToRenameSharedPronunciation -> {
                            queries.renameShared()
                        }
                        else -> {
                        }
                    }
                    setNameInputDialogStatus(Invisible)
                }
            }

            NegativeDialogButtonClicked -> {
                setNameInputDialogStatus(Invisible)
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

    private fun setNameInputDialogStatus(status: NameInputDialogStatus) {
        val databaseValue = nameInputDialogStatusAdapter.encode(status)
        queries.setNameInputDialogStatus(databaseValue)
    }

    private fun getNameInputDialogStatus(): NameInputDialogStatus {
        val databaseValue = queries.getNameInputDialogStatus().executeAsOne()
        return nameInputDialogStatusAdapter.decode(databaseValue)
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