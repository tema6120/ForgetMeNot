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
            queries.isTypedPronunciationNameEmpty().executeAsOne().asBoolean() -> EMPTY
            queries.isTypedPronunciationNameOccupied().executeAsOne().asBoolean() -> OCCUPIED
            else -> OK
        }
        queries.setNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }

    private fun updatePronunciation(
        makeNewPronunciation: (oldPronunciation: Pronunciation.Impl) -> Pronunciation.Impl
    ) {
        val oldPronunciation = queries.getCurrentPronunciation().executeAsOne()
                as Pronunciation.Impl
        val newPronunciation = makeNewPronunciation(oldPronunciation)
        val currentExercisePreference = queries.getCurrentExercisePreference().executeAsOne()
                as ExercisePreference.Impl
        when (determinateChangeType(newPronunciation, currentExercisePreference)) {
            DEFAULT_PRONUNCIATION_BECOMES_INDIVIDUAL -> {
                addNewIndividualPronunciation(newPronunciation)
            }
            DEFAULT_PRONUNCIATION_AND_EXERCISE_PREFERENCE_BECOME_INDIVIDUAL -> {
                addNewIndividualExercisePreference()
                addNewIndividualPronunciation(newPronunciation)
            }
            INDIVIDUAL_PRONUNCIATION_BECOMES_DEFAULT -> {
                // Trigger will set default pronunciationId for ExercisePreference automatically
                queries.deleteCurrentPronunciation()
            }
            INDIVIDUAL_PRONUNCIATION_AND_EXERCISE_PREFERENCE_BECOME_DEFAULT -> {
                queries.deleteCurrentPronunciation()
                queries.deleteCurrentExercisePreference()
            }
            SHARED_PRONUNCIATION_CHANGES, INDIVIDUAL_PRONUNCIATION_CHANGES -> {
                queries.changeCurrentPronunciation(
                    newPronunciation.questionLanguage,
                    newPronunciation.questionAutoSpeak,
                    newPronunciation.answerLanguage,
                    newPronunciation.answerAutoSpeak
                )
            }
        }
    }

    private fun addNewIndividualPronunciation(newPronunciation: Pronunciation) {
        queries.addNewIndividualPronunciation(
            newPronunciation.questionLanguage,
            newPronunciation.questionAutoSpeak,
            newPronunciation.answerLanguage,
            newPronunciation.answerAutoSpeak
        )
        queries.bindNewPronunciationToExercisePreference()
    }

    private fun addNewIndividualExercisePreference() {
        queries.addNewIndividualExercisePreference()
        queries.bindNewExercisePreferenceToDeck()
    }

    private fun determinateChangeType(
        newPronunciation: Pronunciation.Impl,
        currentExercisePreference: ExercisePreference.Impl
    ): ChangeType {
        return when {
            (newPronunciation.id == 0L && currentExercisePreference.id == 0L) -> {
                DEFAULT_PRONUNCIATION_AND_EXERCISE_PREFERENCE_BECOME_INDIVIDUAL
            }
            (newPronunciation.id == 0L) -> {
                DEFAULT_PRONUNCIATION_BECOMES_INDIVIDUAL
            }
            (newPronunciation.name.isNotEmpty()) -> {
                SHARED_PRONUNCIATION_CHANGES
            }
            (shouldBeDefault(newPronunciation)) -> {
                if (shouldBeDefault(currentExercisePreference)) {
                    INDIVIDUAL_PRONUNCIATION_AND_EXERCISE_PREFERENCE_BECOME_DEFAULT
                } else {
                    INDIVIDUAL_PRONUNCIATION_BECOMES_DEFAULT
                }
            }
            else -> INDIVIDUAL_PRONUNCIATION_CHANGES
        }
    }

    private fun shouldBeDefault(pronunciation: Pronunciation.Impl): Boolean {
        val defaultPronunciation = queries.getDefaultPronunciation().executeAsOne()
                as Pronunciation.Impl
        return defaultPronunciation == pronunciation.copy(id = defaultPronunciation.id)
    }

    private fun shouldBeDefault(exercisePreference: ExercisePreference.Impl): Boolean {
        val defaultExercisePreference = queries.getDefaultExercisePreference().executeAsOne()
                as ExercisePreference.Impl
        return defaultExercisePreference == exercisePreference.copy(
            id = defaultExercisePreference.id,
            pronunciationId = defaultExercisePreference.pronunciationId
        )
    }

    private enum class ChangeType {
        DEFAULT_PRONUNCIATION_BECOMES_INDIVIDUAL,
        DEFAULT_PRONUNCIATION_AND_EXERCISE_PREFERENCE_BECOME_INDIVIDUAL,
        INDIVIDUAL_PRONUNCIATION_BECOMES_DEFAULT,
        INDIVIDUAL_PRONUNCIATION_AND_EXERCISE_PREFERENCE_BECOME_DEFAULT,
        SHARED_PRONUNCIATION_CHANGES,
        INDIVIDUAL_PRONUNCIATION_CHANGES
    }

}