package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import java.util.*

class PronunciationSettings(
    private val deckSettings: DeckSettings
) {
    private val currentPronunciation: Pronunciation
        get() = deckSettings.state.deck.exercisePreference.pronunciation

    fun setQuestionLanguage(questionLanguage: Locale?) {
        updatePronunciation(
            isValueChanged = currentPronunciation.questionLanguage != questionLanguage,
            createNewPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    questionLanguage = questionLanguage
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.questionLanguage = questionLanguage
            }
        )
    }

    fun toggleQuestionAutoSpeak() {
        val newQuestionAutoSpeak = !currentPronunciation.questionAutoSpeak
        updatePronunciation(
            isValueChanged = true,
            createNewPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    questionAutoSpeak = newQuestionAutoSpeak
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.questionAutoSpeak = newQuestionAutoSpeak
            }
        )
    }

    fun setAnswerLanguage(answerLanguage: Locale?) {
        updatePronunciation(
            isValueChanged = currentPronunciation.answerLanguage != answerLanguage,
            createNewPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    answerLanguage = answerLanguage
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.answerLanguage = answerLanguage
            }
        )
    }

    fun toggleAnswerAutoSpeak() {
        val newAnswerAutoSpeak = !currentPronunciation.answerAutoSpeak
        updatePronunciation(
            isValueChanged = true,
            createNewPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    answerAutoSpeak = newAnswerAutoSpeak
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.answerAutoSpeak = newAnswerAutoSpeak
            }
        )
    }

    fun toggleSpeakTextInBrackets() {
        val newSpeakTextInBrackets = !currentPronunciation.speakTextInBrackets
        updatePronunciation(
            isValueChanged = true,
            createNewPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    speakTextInBrackets = newSpeakTextInBrackets
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.speakTextInBrackets = newSpeakTextInBrackets
            }
        )
    }

    private inline fun updatePronunciation(
        isValueChanged: Boolean,
        crossinline createNewPronunciation: () -> Pronunciation,
        crossinline updateCurrentPronunciation: () -> Unit
    ) {
        when {
            !isValueChanged -> return
            currentPronunciation.isDefault() -> {
                val newPronunciation = createNewPronunciation()
                deckSettings.setPronunciation(newPronunciation)
            }
            else -> {
                updateCurrentPronunciation()
                if (currentPronunciation.shouldBeDefault()) {
                    deckSettings.setPronunciation(Pronunciation.Default)
                }
            }
        }
    }

    private fun Pronunciation.shallowCopy(
        id: Long,
        questionLanguage: Locale? = this.questionLanguage,
        questionAutoSpeak: Boolean = this.questionAutoSpeak,
        answerLanguage: Locale? = this.answerLanguage,
        answerAutoSpeak: Boolean = this.answerAutoSpeak,
        speakTextInBrackets: Boolean = this.speakTextInBrackets
    ) = Pronunciation(
        id,
        questionLanguage,
        questionAutoSpeak,
        answerLanguage,
        answerAutoSpeak,
        speakTextInBrackets
    )

    private fun Pronunciation.shouldBeDefault(): Boolean =
        this.shallowCopy(id = Pronunciation.Default.id) == Pronunciation.Default
}