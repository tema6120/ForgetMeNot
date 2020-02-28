package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.checkPronunciationName
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings.Event.DeniedPronunciationCreation
import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings.Event.DeniedPronunciationRenaming
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import kotlinx.coroutines.flow.Flow
import java.util.*

class PronunciationSettings(
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState
) {
    sealed class Event {
        class DeniedPronunciationCreation(val nameCheckResult: NameCheckResult) : Event()
        class DeniedPronunciationRenaming(val nameCheckResult: NameCheckResult) : Event()
    }

    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()

    private val exercisePreference: ExercisePreference
        get() = deckSettings.state.deck.exercisePreference

    private val currentPronunciation: Pronunciation
        get() = exercisePreference.pronunciation

    fun setPronunciation(pronunciationId: Long) {
        when (pronunciationId) {
            deckSettings.state.deck.exercisePreference.pronunciation.id -> return
            Pronunciation.Default.id -> deckSettings.setPronunciation(Pronunciation.Default)
            else -> {
                globalState.sharedPronunciations
                    .find { it.id == pronunciationId }
                    ?.let(deckSettings::setPronunciation)
            }
        }
    }

    fun createNewSharedPronunciation(name: String) {
        when (checkPronunciationName(name, globalState)) {
            Ok -> createNewSharedPronunciationAndSetToCurrentExercisePreference(name)
            Empty -> eventFlow.send(DeniedPronunciationCreation(Empty))
            Occupied -> eventFlow.send(DeniedPronunciationCreation(Occupied))
        }
    }

    private fun createNewSharedPronunciationAndSetToCurrentExercisePreference(name: String) {
        val newSharedPronunciation = Pronunciation.Default
            .shallowCopy(id = generateId(), name = name)
        addNewSharedPronunciation(newSharedPronunciation)
        deckSettings.setPronunciation(newSharedPronunciation)
    }

    private fun addNewSharedPronunciation(pronunciation: Pronunciation) {
        globalState.sharedPronunciations =
            (globalState.sharedPronunciations + pronunciation).toCopyableList()
    }

    fun renamePronunciation(pronunciation: Pronunciation, newName: String) {
        when (checkPronunciationName(newName, globalState)) {
            Ok -> {
                when {
                    pronunciation.isDefault() -> {
                        createNewSharedPronunciationAndSetToCurrentExercisePreference(newName)
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
            Empty -> eventFlow.send(DeniedPronunciationRenaming(Empty))
            Occupied -> eventFlow.send(DeniedPronunciationRenaming(Occupied))
        }
    }

    fun deleteSharedPronunciation(pronunciationId: Long) {
        if (pronunciationId == Pronunciation.Default.id) return
        globalState.sharedPronunciations = globalState.sharedPronunciations
            .filter { it.id != pronunciationId }
            .toCopyableList()
        globalState.decks
            .map { it.exercisePreference }
            .filter { it.pronunciation.id == pronunciationId }
            .distinct()
            .forEach { it.pronunciation = Pronunciation.Default }
    }

    fun setQuestionLanguage(questionLanguage: Locale?) {
        updatePronunciation(
            isValueChanged = currentPronunciation.questionLanguage != questionLanguage,
            createNewIndividualPronunciation = {
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

    fun setQuestionAutoSpeak(questionAutoSpeak: Boolean) {
        updatePronunciation(
            isValueChanged = currentPronunciation.questionAutoSpeak != questionAutoSpeak,
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    questionAutoSpeak = questionAutoSpeak
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.questionAutoSpeak = questionAutoSpeak
            }
        )
    }

    fun setAnswerLanguage(answerLanguage: Locale?) {
        updatePronunciation(
            isValueChanged = currentPronunciation.answerLanguage != answerLanguage,
            createNewIndividualPronunciation = {
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

    fun setAnswerAutoSpeak(answerAutoSpeak: Boolean) {
        updatePronunciation(
            isValueChanged = currentPronunciation.answerAutoSpeak != answerAutoSpeak,
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    answerAutoSpeak = answerAutoSpeak
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.answerAutoSpeak = answerAutoSpeak
            }
        )
    }

    fun setDoNotSpeakTextInBrackets(doNotSpeakTextInBrackets: Boolean) {
        updatePronunciation(
            isValueChanged = currentPronunciation.doNotSpeakTextInBrackets != doNotSpeakTextInBrackets,
            createNewIndividualPronunciation = {
                currentPronunciation.shallowCopy(
                    id = generateId(),
                    doNotSpeakTextInBrackets = doNotSpeakTextInBrackets
                )
            },
            updateCurrentPronunciation = {
                currentPronunciation.doNotSpeakTextInBrackets = doNotSpeakTextInBrackets
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
        doNotSpeakTextInBrackets: Boolean = this.doNotSpeakTextInBrackets
    ) = Pronunciation(
        id,
        name,
        questionLanguage,
        questionAutoSpeak,
        answerLanguage,
        answerAutoSpeak,
        doNotSpeakTextInBrackets
    )

    private fun Pronunciation.shouldBeDefault(): Boolean {
        return this.shallowCopy(id = Pronunciation.Default.id) == Pronunciation.Default
    }
}