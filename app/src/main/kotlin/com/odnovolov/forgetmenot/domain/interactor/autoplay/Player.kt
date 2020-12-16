package com.odnovolov.forgetmenot.domain.interactor.autoplay

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.Delay
import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.exercise.TextInBracketsRemover
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class Player(
    val state: State,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    class State(
        playingCards: List<PlayingCard>,
        currentPosition: Int = 0,
        pronunciationEventPosition: Int = 0,
        isPlaying: Boolean = true,
        numberOfLaps: Int,
        currentLap: Int = 0,
        questionSelection: String = "",
        answerSelection: String = ""
    ) : FlowMaker<State>() {
        val playingCards: List<PlayingCard> by flowMaker(playingCards)
        var currentPosition: Int by flowMaker(currentPosition)
        var pronunciationEventPosition: Int by flowMaker(pronunciationEventPosition)
        var isPlaying: Boolean by flowMaker(isPlaying)
        val numberOfLaps: Int by flowMaker(numberOfLaps)
        var currentLap: Int by flowMaker(currentLap)
        var questionSelection: String by flowMaker(questionSelection)
        var answerSelection: String by flowMaker(answerSelection)
    }

    val currentPlayingCard: PlayingCard
        get() = with(state) { playingCards[currentPosition] }

    private lateinit var currentPronunciation: Pronunciation

    private val currentPronunciationPlan
        get() = currentPlayingCard.deck.exercisePreference.pronunciationPlan

    private val currentPronunciationEvent: PronunciationEvent
        get() = currentPronunciationPlan.pronunciationEvents[state.pronunciationEventPosition]

    private val textInBracketsRemover by lazy { TextInBracketsRemover() }

    private var delayJob: Job? = null

    init {
        speaker.setOnSpeakingFinished { tryToExecuteNextPronunciationEvent() }
        updateCurrentPronunciation()
        if (state.isPlaying) {
            executePronunciationEvent()
        }
    }

    fun setCurrentPosition(position: Int) {
        if (position >= state.playingCards.size || position == state.currentPosition) {
            return
        }
        pause()
        state.currentPosition = position
        updateCurrentPronunciation()
        state.pronunciationEventPosition = 0
    }

    private fun updateCurrentPronunciation() {
        val associatedPronunciation = currentPlayingCard.deck.exercisePreference.pronunciation
        currentPronunciation = if (currentPlayingCard.isReverse) {
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
            currentPronunciation.questionLanguage
        )
    }

    private fun speakAnswerSelection() {
        speak(
            state.answerSelection,
            currentPronunciation.answerLanguage
        )
    }

    private fun speakQuestion() {
        with(currentPlayingCard) {
            val question = if (isReverse) card.answer else card.question
            speak(question, currentPronunciation.questionLanguage)
        }
    }

    private fun speakAnswer() {
        with(currentPlayingCard) {
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
        state.isPlaying = true
        executePronunciationEvent()
    }

    private fun executePronunciationEvent() {
        when (val pronunciationEvent = currentPronunciationEvent) {
            SpeakQuestion -> {
                speakQuestion()
            }
            SpeakAnswer -> {
                showAnswer()
                speakAnswer()
            }
            is Delay -> {
                delayJob = launch {
                    delay(pronunciationEvent.timeSpan.millisecondsLong)
                    if (isActive) {
                        tryToExecuteNextPronunciationEvent()
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
                updateCurrentPronunciation()
                state.pronunciationEventPosition = 0
                true
            }
            hasOneMoreLap() -> {
                state.playingCards.forEach { playingCard: PlayingCard ->
                    playingCard.isQuestionDisplayed =
                        playingCard.deck.exercisePreference.isQuestionDisplayed
                    playingCard.isAnswerDisplayed = false
                }
                state.currentPosition = 0
                updateCurrentPronunciation()
                state.pronunciationEventPosition = 0
                state.currentLap++
                true
            }
            else -> false
        }
    }

    private fun hasOneMorePronunciationEventForCurrentPlayingCard(): Boolean =
        state.pronunciationEventPosition + 1 < currentPronunciationPlan.pronunciationEvents.size

    private fun hasOneMorePlayingCard(): Boolean =
        state.currentPosition + 1 < state.playingCards.size

    private fun hasOneMoreLap(): Boolean = state.currentLap + 1 < state.numberOfLaps
}