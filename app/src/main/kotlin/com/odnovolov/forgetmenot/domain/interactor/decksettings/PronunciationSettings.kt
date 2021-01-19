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

    fun toggleQuestionAutoSpeaking() {
        val newQuestionAutoSpeaking = !currentPronunciation.questionAutoSpeaking
        updatePronunciation(
            isValueChanged = true,
            createNewPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    questionAutoSpeaking = newQuestionAutoSpeaking
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.questionAutoSpeaking = newQuestionAutoSpeaking
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

    fun toggleAnswerAutoSpeaking() {
        val newAnswerAutoSpeaking = !currentPronunciation.answerAutoSpeaking
        updatePronunciation(
            isValueChanged = true,
            createNewPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    answerAutoSpeaking = newAnswerAutoSpeaking
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.answerAutoSpeaking = newAnswerAutoSpeaking
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
        questionAutoSpeaking: Boolean = this.questionAutoSpeaking,
        answerLanguage: Locale? = this.answerLanguage,
        answerAutoSpeaking: Boolean = this.answerAutoSpeaking,
        speakTextInBrackets: Boolean = this.speakTextInBrackets
    ) = Pronunciation(
        id,
        questionLanguage,
        questionAutoSpeaking,
        answerLanguage,
        answerAutoSpeaking,
        speakTextInBrackets
    )

    private fun Pronunciation.shouldBeDefault(): Boolean =
        this.shallowCopy(id = Pronunciation.Default.id) == Pronunciation.Default
}