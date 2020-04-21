package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.Delay
import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.exercise.TextInBracketsRemover
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class Repetition(
    val state: State,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    class State(
        repetitionCards: List<RepetitionCard>,
        repetitionCardPosition: Int = 0,
        speakEventPosition: Int = 0,
        isPlaying: Boolean = true,
        numberOfLaps: Int,
        currentLap: Int = 0
    ) : FlowableState<State>() {
        val repetitionCards: List<RepetitionCard> by me(repetitionCards)
        var repetitionCardPosition: Int by me(repetitionCardPosition)
        var speakEventPosition: Int by me(speakEventPosition)
        var isPlaying: Boolean by me(isPlaying)
        val numberOfLaps: Int by me(numberOfLaps)
        var currentLap: Int by me(currentLap)
    }

    private val currentRepetitionCard: RepetitionCard
        get() = with(state) { repetitionCards[repetitionCardPosition] }

    private lateinit var currentPronunciation: Pronunciation

    private val currentSpeakPlan
        get() = currentRepetitionCard.deck.exercisePreference.speakPlan

    private val currentSpeakEvent: SpeakEvent
        get() = currentSpeakPlan.speakEvents[state.speakEventPosition]

    private val textInBracketsRemover by lazy { TextInBracketsRemover() }

    private var delayJob: Job? = null

    init {
        speaker.setOnSpeakingFinished { tryToExecuteNextSpeakEvent() }
        updateCurrentPronunciation()
        if (state.isPlaying) {
            executeSpeakEvent()
        }
    }

    fun setRepetitionCardPosition(position: Int) {
        if (position >= state.repetitionCards.size || position == state.repetitionCardPosition) {
            return
        }
        pause()
        state.repetitionCardPosition = position
        updateCurrentPronunciation()
        state.speakEventPosition = 0
    }

    private fun updateCurrentPronunciation() {
        val associatedPronunciation = currentRepetitionCard.deck.exercisePreference.pronunciation
        currentPronunciation = if (currentRepetitionCard.isReverse) {
            with(associatedPronunciation) {
                Pronunciation(
                    id = -1,
                    name = "",
                    questionLanguage = answerLanguage,
                    questionAutoSpeak = answerAutoSpeak,
                    answerLanguage = questionLanguage,
                    answerAutoSpeak = questionAutoSpeak,
                    speakTextInBrackets = speakTextInBrackets
                )
            }
        } else {
            associatedPronunciation
        }
    }

    fun pause() {
        if (!state.isPlaying) return
        delayJob?.cancel()
        speaker.stop()
        state.isPlaying = false
    }

    fun resume() {
        if (state.isPlaying) return
        state.isPlaying = true
        executeSpeakEvent()
    }

    fun showQuestion() {
        currentRepetitionCard.isQuestionDisplayed = true
    }

    fun showAnswer() {
        showQuestion()
        currentRepetitionCard.isAnswered = true
    }

    fun setIsCardLearned(isLearned: Boolean) {
        currentRepetitionCard.card.isLearned = isLearned
    }

    private fun executeSpeakEvent() {
        when (val speakEvent = currentSpeakEvent) {
            SpeakQuestion -> {
                speakQuestion()
            }
            SpeakAnswer -> {
                showAnswer()
                speakAnswer()
            }
            is Delay -> {
                delayJob = launch {
                    delay(speakEvent.timeSpan.millisecondsLong)
                    if (isActive) {
                        tryToExecuteNextSpeakEvent()
                    }
                }
            }
        }
    }

    fun speakQuestion() {
        with(currentRepetitionCard) {
            val question = if (isReverse) card.answer else card.question
            speak(question, currentPronunciation.questionLanguage)
        }
    }

    fun speakAnswer() {
        with(currentRepetitionCard) {
            val answer = if (isReverse) card.question else card.answer
            speak(answer, currentPronunciation.answerLanguage)
        }
    }

    private fun speak(text: String, language: Locale?) {
        val textToSpeak =
            if (currentPronunciation.speakTextInBrackets) text
            else textInBracketsRemover.process(text)
        speaker.speak(textToSpeak, language)
    }

    private fun tryToExecuteNextSpeakEvent() {
        if (!state.isPlaying) return
        val success: Boolean = switchToNextSpeakEvent()
        if (success) {
            executeSpeakEvent()
        } else {
            state.isPlaying = false
        }
    }

    private fun switchToNextSpeakEvent(): Boolean {
        return when {
            hasOneMoreSpeakEventForCurrentRepetitionCard() -> {
                state.speakEventPosition++
                true
            }
            hasOneMoreRepetitionCard() -> {
                state.repetitionCardPosition++
                updateCurrentPronunciation()
                state.speakEventPosition = 0
                true
            }
            hasOneMoreLap() -> {
                state.repetitionCards.forEach { repetitionCard: RepetitionCard ->
                    repetitionCard.isQuestionDisplayed =
                        repetitionCard.deck.exercisePreference.isQuestionDisplayed
                    repetitionCard.isAnswered = false
                }
                state.repetitionCardPosition = 0
                updateCurrentPronunciation()
                state.speakEventPosition = 0
                state.currentLap++
                true
            }
            else -> false
        }
    }

    private fun hasOneMoreSpeakEventForCurrentRepetitionCard(): Boolean =
        state.speakEventPosition + 1 < currentSpeakPlan.speakEvents.size

    private fun hasOneMoreRepetitionCard(): Boolean =
        state.repetitionCardPosition + 1 < state.repetitionCards.size

    private fun hasOneMoreLap(): Boolean = state.currentLap + 1 < state.numberOfLaps
}