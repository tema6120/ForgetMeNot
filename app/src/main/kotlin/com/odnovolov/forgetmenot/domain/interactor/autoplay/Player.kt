package com.odnovolov.forgetmenot.domain.interactor.autoplay

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.Delay
import com.odnovolov.forgetmenot.domain.interactor.exercise.TextInBracketsRemover
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class Player(
    val state: State,
    private val globalState: GlobalState,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    class State(
        playingCards: List<PlayingCard>,
        currentPosition: Int = 0,
        pronunciationEventPosition: Int = 0,
        isPlaying: Boolean = true,
        isCompleted: Boolean = false,
        questionSelection: String = "",
        answerSelection: String = ""
    ) : FlowMaker<State>() {
        val playingCards: List<PlayingCard> by flowMaker(playingCards)
        var currentPosition: Int by flowMaker(currentPosition)
        var pronunciationEventPosition: Int by flowMaker(pronunciationEventPosition)
        var isPlaying: Boolean by flowMaker(isPlaying)
        var isCompleted: Boolean by flowMaker(isCompleted)
        var questionSelection: String by flowMaker(questionSelection)
        var answerSelection: String by flowMaker(answerSelection)
    }

    val currentPlayingCard: PlayingCard
        get() = with(state) { playingCards[currentPosition] }

    private val currentPronunciation
        get() = currentPlayingCard.deck.exercisePreference.pronunciation

    private val questionLanguage: Locale?
        get() = if (currentPlayingCard.isInverted)
            currentPronunciation.answerLanguage else
            currentPronunciation.questionLanguage

    private val answerLanguage: Locale?
        get() = if (currentPlayingCard.isInverted)
            currentPronunciation.questionLanguage else
            currentPronunciation.answerLanguage

    private val currentPronunciationPlan: PronunciationPlan
        get() = currentPlayingCard.deck.exercisePreference.pronunciationPlan

    private val currentPronunciationEvent: PronunciationEvent
        get() = with(currentPronunciationPlan.pronunciationEvents) {
            getOrElse(state.pronunciationEventPosition) { last() }
        }

    private val textInBracketsRemover by lazy(::TextInBracketsRemover)
    private var delayJob: Job? = null
    private var skipDelay = true

    init {
        speaker.setOnSpeakingFinished(::tryToExecuteNextPronunciationEvent)
        if (state.isPlaying) {
            executePronunciationEvent()
        }
    }

    fun setInfinitePlaybackEnabled(enabled: Boolean) {
        if (globalState.isInfinitePlaybackEnabled == enabled) return
        globalState.isInfinitePlaybackEnabled = enabled
    }

    fun setCurrentPosition(position: Int) {
        if (position >= state.playingCards.size || position == state.currentPosition) {
            return
        }
        pause()
        state.currentPosition = position
        state.pronunciationEventPosition = 0
    }

    fun showQuestion() {
        currentPlayingCard.isQuestionDisplayed = true
    }

    fun showAnswer() {
        showQuestion()
        currentPlayingCard.isAnswerDisplayed = true
    }

    fun setQuestionSelection(selection: String) {
        state.questionSelection = selection
        state.answerSelection = ""
    }

    fun setAnswerSelection(selection: String) {
        state.answerSelection = selection
        state.questionSelection = ""
    }

    fun setIsCardLearned(isLearned: Boolean) {
        currentPlayingCard.card.isLearned = isLearned
    }

    fun speak() {
        pause()
        when {
            hasQuestionSelection() -> speakQuestionSelection()
            hasAnswerSelection() -> speakAnswerSelection()
            currentPlayingCard.isAnswerDisplayed -> speakAnswer()
            else -> speakQuestion()
        }
    }

    private fun hasAnswerSelection(): Boolean = state.answerSelection.isNotEmpty()
    private fun hasQuestionSelection(): Boolean = state.questionSelection.isNotEmpty()

    private fun speakQuestionSelection() {
        speak(
            state.questionSelection,
            questionLanguage
        )
    }

    private fun speakAnswerSelection() {
        speak(
            state.answerSelection,
            answerLanguage
        )
    }

    private fun speakQuestion() {
        with(currentPlayingCard) {
            val question = if (isInverted) card.answer else card.question
            speak(question, questionLanguage)
        }
    }

    private fun speakAnswer() {
        with(currentPlayingCard) {
            val answer = if (isInverted) card.question else card.answer
            speak(answer, answerLanguage)
        }
    }

    private fun speak(text: String, language: Locale?) {
        val textToSpeak =
            if (currentPronunciation.speakTextInBrackets) text
            else textInBracketsRemover.process(text)
        speaker.speak(textToSpeak, language)
    }

    fun stopSpeaking() {
        delayJob?.cancel()
        speaker.stop()
        state.isPlaying = false
    }

    fun setGrade(grade: Int) {
        currentPlayingCard.card.grade = grade
    }

    fun pause() {
        if (!state.isPlaying) return
        delayJob?.cancel()
        speaker.stop()
        state.isPlaying = false
    }

    fun resume() {
        if (state.isPlaying) return
        if (!hasOneMorePronunciationEventForCurrentPlayingCard() && !hasOneMorePlayingCard()) {
            resetProgression()
        }
        skipDelay = true
        state.isPlaying = true
        state.isCompleted = false
        executePronunciationEvent()
    }

    fun playOneMoreLap() {
        resetProgression()
        resume()
    }

    private fun executePronunciationEvent() {
        when (val pronunciationEvent = currentPronunciationEvent) {
            SpeakQuestion -> {
                skipDelay = false
                speakQuestion()
            }
            SpeakAnswer -> {
                skipDelay = false
                showAnswer()
                speakAnswer()
            }
            is Delay -> {
                if (skipDelay) {
                    tryToExecuteNextPronunciationEvent()
                } else {
                    delayJob = launch {
                        delay(pronunciationEvent.timeSpan.millisecondsLong)
                        if (isActive) {
                            tryToExecuteNextPronunciationEvent()
                        }
                    }
                }
            }
        }
    }

    private fun tryToExecuteNextPronunciationEvent() {
        if (!state.isPlaying) return
        val success: Boolean = switchToNextPronunciationEvent()
        if (success) {
            executePronunciationEvent()
        } else {
            state.isCompleted = true
            state.isPlaying = false
        }
    }

    private fun switchToNextPronunciationEvent(): Boolean {
        return when {
            hasOneMorePronunciationEventForCurrentPlayingCard() -> {
                state.pronunciationEventPosition++
                true
            }
            hasOneMorePlayingCard() -> {
                state.currentPosition++
                state.pronunciationEventPosition = 0
                true
            }
            globalState.isInfinitePlaybackEnabled -> {
                resetProgression()
                true
            }
            else -> false
        }
    }

    private fun hasOneMorePronunciationEventForCurrentPlayingCard(): Boolean =
        state.pronunciationEventPosition + 1 < currentPronunciationPlan.pronunciationEvents.size

    private fun hasOneMorePlayingCard(): Boolean =
        state.currentPosition + 1 < state.playingCards.size

    private fun resetProgression() {
        state.playingCards.forEach { playingCard: PlayingCard ->
            with(playingCard) {
                isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed
                isAnswerDisplayed = false
            }
        }
        state.currentPosition = 0
        state.pronunciationEventPosition = 0
    }
}