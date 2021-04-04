package com.odnovolov.forgetmenot.domain.interactor.exercise.example

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose.ToDemonstrateGradingSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose.ToDemonstrateTimerSettings
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class ExampleExercise(
    val state: Exercise.State,
    val purpose: ExerciseExamplePurpose,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    private val textInBracketsRemover by lazy(::TextInBracketsRemover)
    private var timerJob: Job? = null
    private var isExerciseActive = false

    private fun isPositionValid(): Boolean =
        state.currentPosition in 0..state.exerciseCards.lastIndex

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

    private val questionAutoSpeaking: Boolean
        get() = if (currentExerciseCard.base.isInverted)
            currentPronunciation.answerAutoSpeaking else
            currentPronunciation.questionAutoSpeaking

    private val answerAutoSpeaking: Boolean
        get() = if (currentExerciseCard.base.isInverted)
            currentPronunciation.questionAutoSpeaking else
            currentPronunciation.answerAutoSpeaking

    private val grading: Grading
        get() = currentExerciseCard.base.deck.exercisePreference.grading

    private val useTimer: Boolean
        get() = purpose == ToDemonstrateTimerSettings

    fun begin() {
        if (!isPositionValid()) return
        isExerciseActive = true
        autoSpeakQuestionIfNeed()
        startTimer()
    }

    fun end() {
        if (!isPositionValid()) return
        speaker.stop()
        resetTimer()
        isExerciseActive = false
        if (purpose == ToDemonstrateGradingSettings) {
            resetStateForDemonstratingGradingSettings()
        }
    }

    private fun resetStateForDemonstratingGradingSettings() {
        val initialExerciseCard = state.exerciseCards.firstOrNull() ?: return
        initialExerciseCard.base.isAnswerCorrect = null
        initialExerciseCard.base.isQuestionDisplayed =
            initialExerciseCard.base.deck.exercisePreference.isQuestionDisplayed
        initialExerciseCard.base.isGradeEditedManually = false
        initialExerciseCard.base.card.grade = initialExerciseCard.base.initialGrade
        state.currentPosition = 0
        state.exerciseCards = listOf(initialExerciseCard)
    }

    fun notifyExercisePreferenceChanged() {
        if (!isPositionValid()) return
        if (purpose == ToDemonstrateGradingSettings) {
            resetStateForDemonstratingGradingSettings()
        } else {
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
            CardInversion.Randomly -> Random.nextBoolean()
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
            CardInversion.Randomly -> Random.nextBoolean()
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
        if (position < 0
            || position >= state.exerciseCards.size
            || position == state.currentPosition
        ) {
            return
        }
        resetTimer()
        state.currentPosition = position
        autoSpeakQuestionIfNeed()
        startTimer()
    }

    fun showQuestion() {
        if (!isPositionValid()) return
        currentExerciseCard.base.isQuestionDisplayed = true
    }

    fun setQuestionSelection(selection: String) {
        if (!isPositionValid()) return
        state.questionSelection = selection
        state.answerSelection = ""
    }

    fun setAnswerSelection(selection: String) {
        if (!isPositionValid()) return
        state.answerSelection = selection
        state.questionSelection = ""
    }

    fun speak() {
        if (!isPositionValid()) return
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
        if (questionAutoSpeaking && !currentExerciseCard.isAnswered) {
            speakQuestion()
        }
    }

    private fun autoSpeakAnswerIfNeed() {
        if (answerAutoSpeaking && !currentExerciseCard.isAnswered) {
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
        if (!isPositionValid()) return
        speaker.stop()
    }

    fun setGrade(grade: Int) {
        if (purpose != ToDemonstrateGradingSettings) return
        if (!isPositionValid()) return
        if (grade < 0) return
        currentExerciseCard.base.card.grade = grade
        state.exerciseCards.filter { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.id == currentExerciseCard.base.card.id
        }
            .forEach { exerciseCard: ExerciseCard ->
                exerciseCard.base.isGradeEditedManually = true
            }
    }

    fun startTimer() {
        if (!isPositionValid()) return
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
        if (!isPositionValid()) return
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
        if (!isPositionValid()) return
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
        if (!isPositionValid()) return
        currentExerciseCard.let { currentExerciseCard: ExerciseCard ->
            if (currentExerciseCard !is EntryTestExerciseCard || currentExerciseCard.isAnswered) {
                return
            }
            currentExerciseCard.userInput = userInput
        }
    }

    fun answer(answer: Answer) {
        if (!isPositionValid()) return
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
        updateGrade()
    }

    private fun setAnswerAsWrong() {
        if (currentExerciseCard.base.isAnswerCorrect == false) return
        autoSpeakAnswerIfNeed()
        currentExerciseCard.base.isAnswerCorrect = false
        showQuestion()
        addExerciseCardToRetestIfNeed()
        updateGrade()
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
        if (!grading.askAgain) return
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
            if (purpose == ToDemonstrateGradingSettings) {
                ManualTestExerciseCard(baseExerciseCard)
            } else {
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
            }
        state.exerciseCards += retestingExerciseCard
    }

    private fun hasExerciseCardForRetesting(): Boolean {
        return state.exerciseCards
            .drop(state.currentPosition + 1)
            .any { it.base.card.id == currentExerciseCard.base.card.id }
    }

    private fun updateGrade() {
        if (purpose != ToDemonstrateGradingSettings) return
        if (currentExerciseCard.base.isGradeEditedManually) return
        var isFirstAnswer = true
        var calculatingGrade: Int = currentExerciseCard.base.initialGrade
        for (exerciseCard in state.exerciseCards) {
            if (exerciseCard.base.card.id != currentExerciseCard.base.card.id) continue
            calculatingGrade = applyGradeChange(exerciseCard, calculatingGrade, isFirstAnswer)
            isFirstAnswer = false
        }
        currentExerciseCard.base.card.grade = calculatingGrade
    }

    private fun applyGradeChange(
        exerciseCard: ExerciseCard,
        gradeBeforeAnswer: Int,
        isFirstAnswer: Boolean
    ): Int {
        val gradeChange: GradeChange = when (exerciseCard.base.isAnswerCorrect) {
            null -> return gradeBeforeAnswer
            true -> {
                if (isFirstAnswer)
                    grading.onFirstCorrectAnswer else
                    grading.onRepeatedCorrectAnswer
            }
            false -> {
                if (isFirstAnswer)
                    grading.onFirstWrongAnswer else
                    grading.onRepeatedWrongAnswer
            }
        }
        return gradeChange.apply(gradeBeforeAnswer)
    }
}