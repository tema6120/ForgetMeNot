package com.odnovolov.forgetmenot.pronunciation

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.database.Pronunciation
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.listOfLocalesAdapter
import com.odnovolov.forgetmenot.pronunciation.PronunciationController.ChangeType.*
import com.odnovolov.forgetmenot.pronunciation.PronunciationEvent.*

class PronunciationController : BaseController<PronunciationEvent, PronunciationOrder>() {
    val queries: PronunciationControllerQueries = database.pronunciationControllerQueries

    override fun handleEvent(event: PronunciationEvent) {
        when (event) {
            SavePronunciationButtonClicked -> {
                // TODO
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

    private fun updatePronunciation(
        makeNewPronunciation: (oldPronunciation: Pronunciation.Impl) -> Pronunciation
    ) {
        val oldPronunciation = queries.getCurrentPronunciation().executeAsOne()
                as Pronunciation.Impl
        val newPronunciation = makeNewPronunciation(oldPronunciation)
        when (determinateChangeType(newPronunciation)) {
            DEFAULT_BECAME_INDIVIDUAL -> {
                queries.addNewPronunciation(
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