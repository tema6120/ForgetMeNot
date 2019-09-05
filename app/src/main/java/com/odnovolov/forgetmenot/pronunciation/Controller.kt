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

            is PronunciationButtonClicked -> {
                val selectedPronunciation = queries.getPronunciationById(event.pronunciationId)
                    .executeAsOne() as Pronunciation.Impl
                PronunciationUpdater.updateCurrentPronunciation(selectedPronunciation)
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
                // TODO
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
                    val newName = queries.getTypedPronunciationName().executeAsOne()
                    when (getNameInputDialogStatus()) {
                        VisibleToMakeIndividualPronunciationShared -> {
                            PronunciationUpdater.updateCurrentPronunciation {
                                it.copy(name = newName)
                            }
                        }
                        VisibleToCreateNewSharedPronunciation -> {
                            val defaultPronunciation = queries.getDefaultPronunciation()
                                .executeAsOne() as Pronunciation.Impl
                            val newSharedPronunciation = defaultPronunciation.copy(name = newName)
                            PronunciationUpdater.updateCurrentPronunciation(newSharedPronunciation)
                        }
                        VisibleToRenameSharedPronunciation -> {
                            val id = queries.getRenamePronunciationId()
                                .executeAsOneOrNull()?.renamePronunciationId
                            if (id != null) {
                                PronunciationUpdater.renameSharedPronunciation(newName, id)
                            }
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
                PronunciationUpdater.updateCurrentPronunciation {
                    it.copy(questionLanguage = event.language)
                }
            }

            is QuestionAutoSpeakSwitchToggled -> {
                PronunciationUpdater.updateCurrentPronunciation {
                    it.copy(questionAutoSpeak = event.isOn)
                }
            }

            is AnswerLanguageSelected -> {
                PronunciationUpdater.updateCurrentPronunciation {
                    it.copy(answerLanguage = event.language)
                }
            }

            is AnswerAutoSpeakSwitchToggled -> {
                PronunciationUpdater.updateCurrentPronunciation {
                    it.copy(answerAutoSpeak = event.isOn)
                }
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