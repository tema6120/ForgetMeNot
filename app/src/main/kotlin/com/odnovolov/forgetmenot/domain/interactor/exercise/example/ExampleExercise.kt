package com.odnovolov.forgetmenot.domain.interactor.exercise.example

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.CardInversion
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
    val state: Exercise.State,
    private val useTimer: Boolean,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    private val textInBracketsRemover by lazy(::TextInBracketsRemover)
    private var timerJob: Job? = null
    private var isExerciseActive = false

    private val currentExerciseCard: ExerciseCard
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
        isExerciseActive = true
        autoSpeakQuestionIfNeed()
        startTimer()
    }

    fun end() {
        speaker.stop()
        resetTimer()
        isExerciseActive = false
    }

    fun notifyExercisePreferenceChanged() {
        var isExerciseCardsListChanged = false
        val newExerciseCards: List<ExerciseCard> =
            state.exerciseCards.map { exerciseCard: ExerciseCard ->
                if (!exerciseCard.doesCorrespondTestingMethod()) {
                    isExerciseCardsListChanged = true
                    recreateExerciseCard(exerciseCard)
                } else {
                    exerciseCard.apply {
                        conformToExercisePreference()
                    }
                }
            }
        if (isExerciseCardsListChanged) {
            state.exerciseCards = newExerciseCards
        }
        QuizComposer.clearCache()
    }

    private fun ExerciseCard.doesCorrespondTestingMethod(): Boolean =
        when (base.deck.exercisePreference.testingMethod) {
            TestingMethod.Off -> this is OffTestExerciseCard
            TestingMethod.Manual -> this is ManualTestExerciseCard
            TestingMethod.Quiz -> this is QuizTestExerciseCard
            TestingMethod.Entry -> this is EntryTestExerciseCard
        }

    private fun recreateExerciseCard(exerciseCard: ExerciseCard): ExerciseCard {
        val card = exerciseCard.base.card
        val deck = exerciseCard.base.deck
        val isInverted = when (deck.exercisePreference.cardInversion) {
            CardInversion.Off -> false
            CardInversion.On -> true
            CardInversion.EveryOtherLap -> (card.lap % 2) == 1
        }
        val baseExerciseCard = ExerciseCard.Base(
            id = exerciseCard.base.id,
            card = card,
            deck = deck,
            isInverted = isInverted,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            timeLeft = deck.exercisePreference.timeForAnswer,
            initialGrade = card.grade,
            isGradeEditedManually = false
        )
        return when (deck.exercisePreference.testingMethod) {
            TestingMethod.Off -> OffTestExerciseCard(baseExerciseCard)
            TestingMethod.Manual -> ManualTestExerciseCard(baseExerciseCard)
            TestingMethod.Quiz -> {
                val variants: List<Card?> =
                    QuizComposer.compose(card, deck, isInverted, withCaching = true)
                QuizTestExerciseCard(baseExerciseCard, variants)
            }
            TestingMethod.Entry -> {
                EntryTestExerciseCard(baseExerciseCard)
            }
        }
    }

    private fun ExerciseCard.conformToExercisePreference() {
        base.isInverted = when (base.deck.exercisePreference.cardInversion) {
            CardInversion.Off -> false
            CardInversion.On -> true
            CardInversion.EveryOtherLap -> (base.card.lap % 2) == 1
        }
        base.isQuestionDisplayed = base.deck.exercisePreference.isQuestionDisplayed
        base.timeLeft = base.deck.exercisePreference.timeForAnswer
        base.isExpired = false
        base.isAnswerCorrect = null
        when (this) {
            is QuizTestExerciseCard -> selectedVariantIndex = null
            is EntryTestExerciseCard -> userInput = null
        }
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

    private fun speakQuestion() {
        with(currentExerciseCard.base) {
            val question = if (isInverted) card.answer else card.question
            speak(question, questionLanguage)
        }
    }

    private fun speakAnswer() {
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
                || !isExerciseActive
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
                || !isExerciseActive
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
                || !isExerciseActive
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