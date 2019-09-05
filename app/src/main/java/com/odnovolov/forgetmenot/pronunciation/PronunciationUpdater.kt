package com.odnovolov.forgetmenot.pronunciation

import com.odnovolov.forgetmenot.common.database.Pronunciation
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.decksettings.ExercisePreferenceUpdater

object PronunciationUpdater {
    private val queries = database.pronunciationUpdaterQueries

    fun updateCurrentPronunciation(
        makeNewPronunciationOutOfOld: (oldPronunciation: Pronunciation.Impl) -> Pronunciation.Impl
    ) {
        val oldPronunciation = queries.getCurrentPronunciation().executeAsOne()
                as Pronunciation.Impl
        val newPronunciation = makeNewPronunciationOutOfOld(oldPronunciation)
        updateCurrentPronunciation(
            newPronunciation
        )
    }

    fun updateCurrentPronunciation(wishfulPronunciation: Pronunciation.Impl) {
        val newPronunciationId =
            if (shouldPronunciationBeDefault(wishfulPronunciation)) {
                0L
            } else {
                val pronunciationIdToChange = findPronunciationIdToUpdate(wishfulPronunciation)
                if (pronunciationIdToChange != null) {
                    with(wishfulPronunciation) {
                        queries.changePronunciation(
                            name,
                            questionLanguage,
                            questionAutoSpeak,
                            answerLanguage,
                            answerAutoSpeak,
                            where = pronunciationIdToChange
                        )
                    }
                    pronunciationIdToChange
                } else {
                    with(wishfulPronunciation) {
                        queries.addNewPronunciation(
                            name,
                            questionLanguage,
                            questionAutoSpeak,
                            answerLanguage,
                            answerAutoSpeak
                        )
                    }
                    queries.getLastInsertId().executeAsOne()
                }
            }
        val currentPronunciationId = queries.getCurrentPronunciationId().executeAsOne()
        if (currentPronunciationId != newPronunciationId) {
            ExercisePreferenceUpdater.updateCurrentExercisePreference {
                it.copy(pronunciationId = newPronunciationId)
            }
            queries.deleteUnusedIndividualPronunciation()
        }
    }

    private fun shouldPronunciationBeDefault(pronunciation: Pronunciation.Impl): Boolean {
        val defaultPronunciation = queries.getDefaultPronunciation().executeAsOne()
                as Pronunciation.Impl
        return defaultPronunciation == pronunciation.copy(id = defaultPronunciation.id)
    }

    private fun findPronunciationIdToUpdate(sourcePronunciation: Pronunciation): Long? {
        return if (sourcePronunciation.name.isNotEmpty()) {
            queries.getPronunciationIdByName(sourcePronunciation.name).executeAsOneOrNull()
        } else {
            queries.getCurrentPronunciationIdIfItIsIndividual().executeAsOneOrNull()
        }
    }
}