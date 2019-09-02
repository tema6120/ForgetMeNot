package com.odnovolov.forgetmenot.pronunciation

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.NameCheckResult
import com.odnovolov.forgetmenot.common.NameCheckResult.*
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.pronunciation.PronunciationController.ChangeType.*
import com.odnovolov.forgetmenot.pronunciation.PronunciationEvent.*

class PronunciationController : BaseController<PronunciationEvent, PronunciationOrder>() {
    val queries: PronunciationControllerQueries = database.pronunciationControllerQueries

    override fun handleEvent(event: PronunciationEvent) {
        when (event) {
            SavePronunciationButtonClicked -> {
                queries.setWaitingForNameToSavePronunciation(true)
            }

            AddNewPronunciationButtonClicked -> {
                queries.setWaitingForNameToCreateNewPronunciation(true)
            }

            is DialogTextChanged -> {
                queries.setTypedPronunciationName(event.text)
                checkName()
            }

            PositiveDialogButtonClicked -> {
                val nameCheckResult = checkName()
                if (nameCheckResult === OK) {
                    val newName = queries.getTypedPronunciationName().executeAsOne()
                    when {
                        queries.isWaitingForNameToSavePronunciation().executeAsOne() -> {
                            val currentPronunciationId =
                                queries.getCurrentPronunciationId().executeAsOne()
                            queries.rename(newName, currentPronunciationId)
                            queries.setWaitingForNameToSavePronunciation(false)
                        }
                        queries.isWaitingForNameToCreateNewPronunciation().executeAsOne() -> {
                            queries.addNewSharedPronunciation()
                            queries.bindNewPronunciationToExercisePreference()
                            queries.setWaitingForNameToCreateNewPronunciation(false)
                        }
                    }
                }
            }

            NegativeDialogButtonClicked -> {
                queries.setWaitingForNameToSavePronunciation(false)
                queries.setWaitingForNameToCreateNewPronunciation(false)
            }

            is AvailableLanguagesUpdated -> {
                val availableLanguages = listOfLocalesAdapter.encode(event.languages.toList())
                queries.setAvailableLanguages(availableLanguages)
            }

            is QuestionLanguageSelected -> {
                updatePronunciation { it.copy(questionLanguage = event.language) }
            }

            is QuestionAutoSpeakSwitchToggled -> {
                updatePronunciation { it.copy(questionAutoSpeak = event.isOn) }
            }

            is AnswerLanguageSelected -> {
                updatePronunciation { it.copy(answerLanguage = event.language) }
            }

            is AnswerAutoSpeakSwitchToggled -> {
                updatePronunciation { it.copy(answerAutoSpeak = event.isOn) }
            }
        }
    }

    private fun checkName(): NameCheckResult {
        val nameCheckResult = when {
            isTypedPronunciationNameEmpty() -> EMPTY
            isTypedPronunciationNameOccupied() -> OCCUPIED
            else -> OK
        }
        queries.setNameCheckResult(nameCheckStatusAdapter.encode(nameCheckResult))
        return nameCheckResult
    }

    private fun isTypedPronunciationNameEmpty(): Boolean {
        return queries.isTypedPronunciationNameEmpty().executeAsOne().asBoolean()
    }

    private fun isTypedPronunciationNameOccupied(): Boolean {
        return queries.isTypedPronunciationNameOccupied().executeAsOne().asBoolean()
    }

    private fun updatePronunciation(
        makeNewPronunciation: (oldPronunciation: Pronunciation.Impl) -> Pronunciation
    ) {
        val oldPronunciation = queries.getCurrentPronunciation().executeAsOne()
                as Pronunciation.Impl
        val newPronunciation = makeNewPronunciation(oldPronunciation)
        when (determinateChangeType(newPronunciation)) {
            DEFAULT_BECAME_INDIVIDUAL -> {
                queries.addNewIndividualPronunciation(
                    newPronunciation.questionLanguage,
                    newPronunciation.questionAutoSpeak,
                    newPronunciation.answerLanguage,
                    newPronunciation.answerAutoSpeak
                )
                queries.bindNewPronunciationToExercisePreference()
            }
            INDIVIDUAL_BECAME_DEFAULT -> {
                // Trigger will set default pronunciationId for ExercisePreference automatically
                queries.deletePronunciation(newPronunciation.id)
            }
            SHARED_CHANGED, INDIVIDUAL_CHANGED -> {
                queries.changePronunciation(
                    newPronunciation.questionLanguage,
                    newPronunciation.questionAutoSpeak,
                    newPronunciation.answerLanguage,
                    newPronunciation.answerAutoSpeak,
                    newPronunciation.id
                )
            }
        }
    }

    private fun determinateChangeType(newPronunciation: Pronunciation): ChangeType {
        return when {
            (newPronunciation.id == 0L) -> DEFAULT_BECAME_INDIVIDUAL
            (newPronunciation.name.isNotEmpty()) -> SHARED_CHANGED
            (newPronunciation.questionLanguage == null
                    && !newPronunciation.questionAutoSpeak
                    && newPronunciation.answerLanguage == null
                    && !newPronunciation.answerAutoSpeak) -> INDIVIDUAL_BECAME_DEFAULT
            else -> INDIVIDUAL_CHANGED
        }
    }

    private enum class ChangeType {
        DEFAULT_BECAME_INDIVIDUAL,
        INDIVIDUAL_BECAME_DEFAULT,
        SHARED_CHANGED,
        INDIVIDUAL_CHANGED
    }

}