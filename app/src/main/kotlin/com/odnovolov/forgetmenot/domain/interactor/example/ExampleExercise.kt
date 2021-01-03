package com.odnovolov.forgetmenot.domain.interactor.example

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.entity.TestingMethod
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class ExampleExercise(
    private val stateCreator: ExampleExerciseStateCreator,
    state: Exercise.State?,
    private val useTimer: Boolean,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    val state: Exercise.State = state ?: stateCreator.create()
    private val textInBracketsRemover by lazy(::TextInBracketsRemover)
    private var timerJob: Job? = null

    val currentExerciseCard: ExerciseCard
        get() = state.exerciseCards[state.currentPosition]

    private val currentPronunciation
        get() = currentExerciseCard.base.deck.exercisePreference.pronunciation

    private val questionLanguage: Locale?
        get() = if (currentExerciseCard.base.isInverted)
            currentPronunciation.answerLanguage else
            currentPronunciation.questionLanguage

    private val answerLanguage: Locale?
        get() = if (currentExerciseCard.base.isInverted)
            currentPronunciation.questionLanguage else
            currentPronunciation.answerLanguage

    private val questionAutoSpeak: Boolean
        get() = if (currentExerciseCard.base.isInverted)
            currentPronunciation.answerAutoSpeak else
            currentPronunciation.questionAutoSpeak

    private val answerAutoSpeak: Boolean
        get() = if (currentExerciseCard.base.isInverted)
            currentPronunciation.questionAutoSpeak else
            currentPronunciation.answerAutoSpeak

    fun begin() {
        autoSpeakQuestionIfNeed()
        startTimer()
    }

    fun end() {
        speaker.stop()
    }

    fun notifyExercisePreferenceChanged() {
        val newState = stateCreator.create()
        state.questionSelection = newState.questionSelection
        state.answerSelection = newState.answerSelection
        state.currentPosition = newState.currentPosition
        state.exerciseCards = newState.exerciseCards
    }

    fun setPosition(position: Int) {
        if (position >= state.exerciseCards.size || position == state.currentPosition) {
            return
        }
        resetTimer()
        state.currentPosition = position
        autoSpeakQuestionIfNeed()
        startTimer()
    }

    fun showQuestion() {
        currentExerciseCard.base.isQuestionDisplayed = true
    }

    fun setQuestionSelection(selection: String) {
        state.questionSelection = selection
        state.answerSelection = ""
    }

    fun setAnswerSelection(selection: String) {
        state.answerSelection = selection
        state.questionSelection = ""
    }

    fun speak() {
        when {
            hasQuestionSelection() -> speakQuestionSelection()
            hasAnswerSelection() -> speakAnswerSelection()
            currentExerciseCard.isAnswered -> speakAnswer()
            else -> speakQuestion()
        }
    }

    private fun hasQuestionSelection(): Boolean = state.questionSelection.isNotEmpty()
    private fun hasAnswerSelection(): Boolean = state.answerSelection.isNotEmpty()

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

    private fun autoSpeakQuestionIfNeed() {
        if (questionAutoSpeak && !currentExerciseCard.isAnswered) {
            speakQuestion()
        }
    }

    private fun autoSpeakAnswerIfNeed() {
        if (answerAutoSpeak && !currentExerciseCard.isAnswered) {
            speakAnswer()
        }
    }

    fun speakQuestion() {
        with(currentExerciseCard.base) {
            val question = if (isInverted) card.answer else card.question
            speak(question, questionLanguage)
        }
    }

    fun speakAnswer() {
        with(currentExerciseCard.base) {
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
        speaker.stop()
    }

    fun startTimer() {
        with(currentExerciseCard.base) {
            if (!useTimer
                || currentExerciseCard.isAnswered
                || isExpired
                || timeLeft <= 0
                || timerJob?.isActive == true
            ) {
                return
            }
            timerJob = launch {
                while (timeLeft > 0) {
                    delay(1000)
                    timeLeft--
                }
                if (isActive) {
                    isExpired = true
                    setAnswerAsWrong()
                }
            }
        }
    }

    fun resetTimer() {
        with(currentExerciseCard.base) {
            if (!useTimer
                || isExpired
                || currentExerciseCard.isAnswered
            ) {
                return
            }
            timerJob?.cancel()
            timeLeft = deck.exercisePreference.timeForAnswer
        }
    }

    fun stopTimer() {
        with(currentExerciseCard.base) {
            if (!useTimer
                || isExpired
                || currentExerciseCard.isAnswered
            ) {
                return
            }
            timerJob?.cancel()
            timeLeft = 0
        }
    }

    fun setUserInput(userInput: String?) {
        currentExerciseCard.let { currentExerciseCard: ExerciseCard ->
            if (currentExerciseCard !is EntryTestExerciseCard || currentExerciseCard.isAnswered) {
                return
            }
            currentExerciseCard.userInput = userInput
        }
    }

    fun answer(answer: Answer) {
        if (!isAnswerRelevant(answer)) return
        stopTimer()
        when (answer) {
            Show, Remember -> setAnswerAsCorrect()
            NotRemember -> setAnswerAsWrong()
            is Variant -> checkVariant(answer.variantIndex)
            Entry -> checkEntry()
        }
    }

    private fun isAnswerRelevant(answer: Answer): Boolean {
        return when (answer) {
            Show, Remember, NotRemember -> currentExerciseCard is OffTestExerciseCard
                    || currentExerciseCard is ManualTestExerciseCard
            is Variant -> currentExerciseCard is QuizTestExerciseCard
            Entry -> currentExerciseCard is EntryTestExerciseCard
        }
    }

    private fun checkVariant(variantIndex: Int) {
        val quizExerciseCard = currentExerciseCard as QuizTestExerciseCard
        if (quizExerciseCard.selectedVariantIndex != null
            || variantIndex >= quizExerciseCard.variants.size
        ) return
        quizExerciseCard.selectedVariantIndex = variantIndex
        val selectedCardId: Long? = quizExerciseCard.variants[variantIndex]?.id
        val isVariantCorrect = selectedCardId == quizExerciseCard.base.card.id
        if (isVariantCorrect)
            setAnswerAsCorrect() else
            setAnswerAsWrong()
    }

    private fun checkEntry() {
        val entryExerciseCard = currentExerciseCard as EntryTestExerciseCard
        val correctAnswer = with(entryExerciseCard.base) {
            if (isInverted) card.question else card.answer
        }
        val isUserAnswerCorrect = entryExerciseCard.userInput?.trim() == correctAnswer.trim()
        if (isUserAnswerCorrect)
            setAnswerAsCorrect() else
            setAnswerAsWrong()
    }

    private fun setAnswerAsCorrect() {
        if (currentExerciseCard.base.isAnswerCorrect == true) return
        autoSpeakAnswerIfNeed()
        currentExerciseCard.base.isAnswerCorrect = true
        showQuestion()
        deleteCardsForRetesting()
    }

    private fun setAnswerAsWrong() {
        if (currentExerciseCard.base.isAnswerCorrect == false) return
        autoSpeakAnswerIfNeed()
        currentExerciseCard.base.isAnswerCorrect = false
        showQuestion()
        addExerciseCardToRetestIfNeed()
    }

    private fun deleteCardsForRetesting() {
        if (hasExerciseCardForRetesting()) {
            state.exerciseCards = state.exerciseCards
                .filterIndexed { index, exerciseCard ->
                    exerciseCard.base.card.id != currentExerciseCard.base.card.id
                            || exerciseCard.base.card.id == currentExerciseCard.base.card.id
                            && index <= state.currentPosition
                }
        }
    }

    private fun addExerciseCardToRetestIfNeed() {
        if (hasExerciseCardForRetesting()) return
        val baseExerciseCard = with(currentExerciseCard.base) {
            ExerciseCard.Base(
                id = generateId(),
                card = card,
                deck = deck,
                isInverted = isInverted,
                isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
                timeLeft = deck.exercisePreference.timeForAnswer,
                initialGrade = initialGrade,
                isGradeEditedManually = isGradeEditedManually
            )
        }
        val retestingExerciseCard: ExerciseCard =
            when (currentExerciseCard.base.deck.exercisePreference.testingMethod) {
                TestingMethod.Off -> OffTestExerciseCard(baseExerciseCard)
                TestingMethod.Manual -> ManualTestExerciseCard(baseExerciseCard)
                TestingMethod.Quiz -> {
                    val variants: List<Card?> = with(baseExerciseCard) {
                        QuizComposer.compose(card, deck, isInverted, withCaching = false)
                    }
                    QuizTestExerciseCard(baseExerciseCard, variants)
                }
                TestingMethod.Entry -> EntryTestExerciseCard(baseExerciseCard)
            }
        state.exerciseCards += retestingExerciseCard
    }

    private fun hasExerciseCardForRetesting(): Boolean {
        return state.exerciseCards
            .drop(state.currentPosition + 1)
            .any { it.base.card.id == currentExerciseCard.base.card.id }
    }
}