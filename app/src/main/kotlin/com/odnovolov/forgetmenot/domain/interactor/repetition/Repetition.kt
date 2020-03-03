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
    private val coroutineDispatcher: CoroutineDispatcher
) : CoroutineScope {
    class State(
        repetitionCards: List<RepetitionCard>,
        repetitionCardPosition: Int = 0,
        speakEventPosition: Int = 0,
        isPlaying: Boolean = true
    ) : FlowableState<State>() {
        val repetitionCards: List<RepetitionCard> by me(repetitionCards)
        var repetitionCardPosition: Int by me(repetitionCardPosition)
        var speakEventPosition: Int by me(speakEventPosition)
        var isPlaying: Boolean by me(isPlaying)
    }

    private val currentRepetitionCard: RepetitionCard
        get() = with(state) {
            repetitionCards[repetitionCardPosition]
        }

    private val currentSpeakEvent: SpeakEvent
        get() = currentRepetitionCard.speakPlan.speakEvents[state.speakEventPosition]

    private val currentPronunciation: Pronunciation
        get() {
            return if (currentRepetitionCard.isReverse) {
                with(currentRepetitionCard.pronunciation) {
                    Pronunciation(
                        id = -1,
                        name = "",
                        questionLanguage = answerLanguage,
                        questionAutoSpeak = answerAutoSpeak,
                        answerLanguage = questionLanguage,
                        answerAutoSpeak = questionAutoSpeak,
                        doNotSpeakTextInBrackets = doNotSpeakTextInBrackets
                    )
                }
            } else {
                currentRepetitionCard.pronunciation
            }
        }

    private val textInBracketsRemover by lazy { TextInBracketsRemover() }

    private var delayJob: Job? = null

    override val coroutineContext: CoroutineContext
        get() = Job() + coroutineDispatcher

    init {
        speaker.setOnSpeakingFinished { tryToExecuteNextSpeakEvent() }
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
        state.speakEventPosition = 0
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

    fun showAnswer() {
        if (!currentRepetitionCard.isAnswered) {
            currentRepetitionCard.isAnswered = true
        }
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
                    delay(speakEvent.seconds * 1000L)
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
        val textToSpeak = if (currentPronunciation.doNotSpeakTextInBrackets) {
            textInBracketsRemover.process(text)
        } else {
            text
        }
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
                state.speakEventPosition = 0
                true
            }
            else -> false
        }
    }

    private fun hasOneMoreSpeakEventForCurrentRepetitionCard(): Boolean =
        state.speakEventPosition + 1 < currentRepetitionCard.speakPlan.speakEvents.size

    private fun hasOneMoreRepetitionCard(): Boolean =
        state.repetitionCardPosition + 1 < state.repetitionCards.size
}