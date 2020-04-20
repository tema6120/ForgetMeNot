package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.generateId
import java.util.*

class PronunciationSettings(
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState
) {
    private val currentPronunciation: Pronunciation
        get() = deckSettings.state.deck.exercisePreference.pronunciation

    fun setPronunciation(pronunciationId: Long) {
        when (pronunciationId) {
            currentPronunciation.id -> return
            Pronunciation.Default.id -> deckSettings.setPronunciation(Pronunciation.Default)
            else -> {
                globalState.sharedPronunciations
                    .find { it.id == pronunciationId }
                    ?.let(deckSettings::setPronunciation)
            }
        }
    }

    fun createNewSharedPronunciation(name: String) {
        checkName(name)
        createNewSharedPronunciationAndSetAsCurrent(name)
    }

    private fun checkName(testedName: String) {
        when (checkPronunciationName(testedName, globalState)) {
            Ok -> return
            Empty -> throw IllegalArgumentException("shared Pronunciation name cannot be empty")
            Occupied -> throw IllegalArgumentException("$testedName is occupied")
        }
    }

    private fun createNewSharedPronunciationAndSetAsCurrent(name: String) {
        val newSharedPronunciation: Pronunciation = Pronunciation.Default
            .shallowCopy(id = generateId(), name = name)
        addNewSharedPronunciation(newSharedPronunciation)
        deckSettings.setPronunciation(newSharedPronunciation)
    }

    private fun addNewSharedPronunciation(pronunciation: Pronunciation) {
        globalState.sharedPronunciations =
            (globalState.sharedPronunciations + pronunciation).toCopyableList()
    }

    fun renamePronunciation(pronunciation: Pronunciation, newName: String) {
        checkName(newName)
        when {
            pronunciation.isDefault() -> {
                createNewSharedPronunciationAndSetAsCurrent(newName)
            }
            pronunciation.isIndividual() -> {
                pronunciation.name = newName
                addNewSharedPronunciation(pronunciation)
            }
            else -> { // current Pronunciation is shared
                pronunciation.name = newName
            }
        }
    }

    fun deleteSharedPronunciation(pronunciationId: Long) {
        if (pronunciationId == Pronunciation.Default.id) return
        globalState.sharedPronunciations = globalState.sharedPronunciations
            .filter { sharedPronunciation -> sharedPronunciation.id != pronunciationId }
            .toCopyableList()
        globalState.decks
            .map(Deck::exercisePreference)
            .filter { exercisePreference -> exercisePreference.pronunciation.id == pronunciationId }
            .distinct()
            .forEach { exercisePreference ->
                exercisePreference.pronunciation = Pronunciation.Default
            }
        deckSettings.recheckIndividualExercisePreferences()
    }

    fun setQuestionLanguage(questionLanguage: Locale?) {
        updatePronunciation(
            isValueChanged = currentPronunciation.questionLanguage != questionLanguage,
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    name = "",
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
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    name = "",
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
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    name = "",
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
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    name = "",
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
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    name = "",
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
        createNewIndividualPronunciation: () -> Pronunciation,
        updateCurrentPronunciation: () -> Unit
    ) {
        when {
            !isValueChanged -> return
            currentPronunciation.isDefault() -> {
                val newIndividualPronunciation = createNewIndividualPronunciation()
                deckSettings.setPronunciation(newIndividualPronunciation)
            }
            currentPronunciation.isIndividual() -> {
                updateCurrentPronunciation()
                if (currentPronunciation.shouldBeDefault()) {
                    deckSettings.setPronunciation(Pronunciation.Default)
                }
            }
            else -> { // current Pronunciation is shared
                updateCurrentPronunciation()
            }
        }
    }

    private fun Pronunciation.shallowCopy(
        id: Long,
        name: String = this.name,
        questionLanguage: Locale? = this.questionLanguage,
        questionAutoSpeak: Boolean = this.questionAutoSpeak,
        answerLanguage: Locale? = this.answerLanguage,
        answerAutoSpeak: Boolean = this.answerAutoSpeak,
        speakTextInBrackets: Boolean = this.speakTextInBrackets
    ) = Pronunciation(
        id,
        name,
        questionLanguage,
        questionAutoSpeak,
        answerLanguage,
        answerAutoSpeak,
        speakTextInBrackets
    )

    private fun Pronunciation.shouldBeDefault(): Boolean =
        this.shallowCopy(id = Pronunciation.Default.id) == Pronunciation.Default
}