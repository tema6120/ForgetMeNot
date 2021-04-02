package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.*
import com.soywiz.klock.DateTime
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class Exercise(
    val state: State,
    private val globalState: GlobalState,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    class State(
        exerciseCards: List<ExerciseCard>,
        currentPosition: Int = 0,
        questionSelection: String = "",
        answerSelection: String = "",
        hintSelection: HintSelection = HintSelection(0, 0)
    ) : FlowMaker<State>() {
        var exerciseCards: List<ExerciseCard> by flowMaker(exerciseCards)
        var currentPosition: Int by flowMaker(currentPosition)
        var questionSelection: String by flowMaker(questionSelection)
        var answerSelection: String by flowMaker(answerSelection)
        var hintSelection: HintSelection by flowMaker(hintSelection)
    }

    private val textInBracketsRemover by lazy(::TextInBracketsRemover)
    private val exerciseCardConformer = ExerciseCardConformer(state, globalState)
    private var timerJob: Job? = null

    private val isWalkingMode
        get() = globalState.isWalkingModeEnabled

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

    init {
        if (isPositionValid()) {
            autoSpeakQuestionIfNeed()
            updateLastOpenedAt()
            startTimer()
        }
    }

    private fun isPositionValid(): Boolean =
        state.currentPosition in 0..state.exerciseCards.lastIndex

    fun setCurrentPosition(position: Int) {
        if (position < 0
            || position >= state.exerciseCards.size
            || position == state.currentPosition
        ) {
            return
        }
        resetTimer()
        state.currentPosition = position
        autoSpeakQuestionIfNeed()
        updateLastOpenedAt()
        startTimer()
    }

    private fun updateLastOpenedAt() {
        if (!isPositionValid()) return
        currentExerciseCard.base.deck.lastTestedAt = DateTime.now()
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

    fun setIsCardLearned(isLearned: Boolean) {
        if (!isPositionValid()) return
        if (currentExerciseCard.base.card.isLearned == isLearned) return
        currentExerciseCard.base.card.isLearned = isLearned
        if (isLearned) {
            deleteCardsForRetesting()
            stopTimer()
        } else {
            resetTimer()
            if (currentExerciseCard.base.isAnswerCorrect == false) {
                updateGrade()
                addExerciseCardToRetestIfNeed()
            }
            startTimer()
        }
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
        if (isWalkingMode
            || questionAutoSpeaking
            && !currentExerciseCard.base.card.isLearned
            && !currentExerciseCard.isAnswered
        ) {
            speakQuestion()
        }
    }

    private fun autoSpeakAnswerIfNeed() {
        if (isWalkingMode
            || answerAutoSpeaking
            && !currentExerciseCard.base.card.isLearned
            && !currentExerciseCard.isAnswered
        ) {
            speakAnswer()
        }
    }

    fun speakQuestion() {
        if (!isPositionValid()) return
        with(currentExerciseCard.base) {
            val question = if (isInverted) card.answer else card.question
            speak(question, questionLanguage)
        }
    }

    fun speakAnswer() {
        if (!isPositionValid()) return
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

    fun setWalkingModeEnabled(enabled: Boolean) {
        if (isWalkingMode == enabled) return
        if (!isPositionValid()) {
            globalState.isWalkingModeEnabled = enabled
        } else {
            stopTimer()
            globalState.isWalkingModeEnabled = enabled
            exerciseCardConformer.conform()
            startTimer()
        }
    }

    fun setGrade(grade: Int) {
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

    fun setHintSelection(startIndex: Int, endIndex: Int) {
        if (!isPositionValid()) return
        state.hintSelection = HintSelection(startIndex, endIndex)
    }

    fun showHint() {
        if (!isPositionValid()) return
        val hasHint: Boolean = currentExerciseCard.base.hint != null
        val hasHintSelection: Boolean =
            state.hintSelection.endIndex - state.hintSelection.startIndex > 0
        val answer: String = with(currentExerciseCard.base) {
            if (isInverted) card.question else card.answer
        }
        val oldHint: String? = currentExerciseCard.base.hint
        currentExerciseCard.base.hint = when {
            !hasHint -> Prompter.maskLetters(answer)
            hasHintSelection ->
                Prompter.unmaskRange(
                    answer,
                    oldHint!!,
                    state.hintSelection.startIndex,
                    state.hintSelection.endIndex
                )
            else -> Prompter.unmaskFirst(answer, oldHint!!)
        }
    }

    fun getVariants() {
        if (!isPositionValid()) return
        if (isWalkingMode
            || currentExerciseCard is QuizTestExerciseCard
            || currentExerciseCard.base.card.isLearned
            || currentExerciseCard.isAnswered
        ) {
            return
        }
        resetTimer()
        val variants: List<Card?> = with(currentExerciseCard.base) {
            QuizComposer.compose(card, deck, isInverted, withCaching = false)
        }
        val newQuizTestExerciseCard = QuizTestExerciseCard(currentExerciseCard.base, variants)
        state.exerciseCards = state.exerciseCards.toMutableList().run {
            this[state.currentPosition] = newQuizTestExerciseCard
            toList()
        }
        startTimer()
    }

    fun startTimer() {
        if (!isPositionValid()) return
        with(currentExerciseCard.base) {
            if (isWalkingMode
                || card.isLearned
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
            if (isWalkingMode
                || card.isLearned
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
            if (isWalkingMode
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
        if (isVariantCorrect) setAnswerAsCorrect()
        else setAnswerAsWrong()
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
        incrementLapIfCardIsAnsweredForTheFirstTime()
        currentExerciseCard.base.isAnswerCorrect = true
        showQuestion()
        deleteCardsForRetesting()
        updateGrade()
        updateLastAnsweredAt()
    }

    private fun setAnswerAsWrong() {
        if (currentExerciseCard.base.isAnswerCorrect == false) return
        autoSpeakAnswerIfNeed()
        incrementLapIfCardIsAnsweredForTheFirstTime()
        currentExerciseCard.base.isAnswerCorrect = false
        showQuestion()
        addExerciseCardToRetestIfNeed()
        updateGrade()
        updateLastAnsweredAt()
    }

    private fun incrementLapIfCardIsAnsweredForTheFirstTime() {
        val isAnsweredForTheFirstTime = state.exerciseCards
            .any { exerciseCard: ExerciseCard ->
                exerciseCard.base.card.id == currentExerciseCard.base.card.id
                        && exerciseCard.isAnswered
            }
            .not()
        if (isAnsweredForTheFirstTime) {
            currentExerciseCard.base.card.lap++
        }
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
                timeLeft = if (isWalkingMode) DO_NOT_USE_TIMER else deck.exercisePreference.timeForAnswer,
                initialGrade = initialGrade,
                isGradeEditedManually = isGradeEditedManually
            )
        }
        val retestingExerciseCard: ExerciseCard =
            when (currentExerciseCard.base.deck.exercisePreference.testingMethod) {
                TestingMethod.Off -> OffTestExerciseCard(baseExerciseCard)
                TestingMethod.Manual -> ManualTestExerciseCard(baseExerciseCard)
                TestingMethod.Quiz -> {
                    if (isWalkingMode) {
                        ManualTestExerciseCard(baseExerciseCard)
                    } else {
                        val variants: List<Card?> = with(baseExerciseCard) {
                            QuizComposer.compose(card, deck, isInverted, withCaching = false)
                        }
                        QuizTestExerciseCard(baseExerciseCard, variants)
                    }
                }
                TestingMethod.Entry -> {
                    if (isWalkingMode) {
                        ManualTestExerciseCard(baseExerciseCard)
                    } else {
                        EntryTestExerciseCard(baseExerciseCard)
                    }
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

    private fun updateLastAnsweredAt() {
        currentExerciseCard.base.card.lastTestedAt = DateTime.now()
    }

    fun notifyCardsRemoved(removedCardIds: List<Long>) {
        state.exerciseCards = state.exerciseCards
            .filter { exerciseCard: ExerciseCard -> exerciseCard.base.card.id !in removedCardIds }
    }

    fun notifyCardsMoved(cardMovement: List<CardMoving>) {
        var isExercisePreferenceChanged = false
        for (cardMoving: CardMoving in cardMovement) {
            for (exerciseCard: ExerciseCard in state.exerciseCards) {
                if (exerciseCard.base.card.id != cardMoving.cardId) continue
                if (exerciseCard.base.deck.exercisePreference.id
                    != cardMoving.deckMovedTo.exercisePreference.id
                ) {
                    isExercisePreferenceChanged = true
                }
                exerciseCard.base.deck = cardMoving.deckMovedTo
            }
        }
        if (isExercisePreferenceChanged) {
            exerciseCardConformer.conform()
        }
    }

    fun notifyExercisePreferenceChanged() {
        if (!isPositionValid()) return
        exerciseCardConformer.conform()
    }

    fun notifyCardChanged(
        cardId: Long,
        isQuestionChanged: Boolean,
        isAnswerChanged: Boolean,
        isGradeChanged: Boolean,
        isIsLearnedChanged: Boolean
    ) {
        if (!isPositionValid()) return
        val currentPosition = state.currentPosition
        for ((position: Int, exerciseCard: ExerciseCard) in state.exerciseCards.withIndex()) {
            if (cardId != exerciseCard.base.card.id) continue
            state.currentPosition = position
            val isReverse = exerciseCard.base.isInverted
            val isActualAnswerChanged: Boolean =
                isReverse && isQuestionChanged || !isReverse && isAnswerChanged
            if (isActualAnswerChanged) {
                exerciseCard.base.hint = null
                if (exerciseCard is EntryTestExerciseCard && exerciseCard.isAnswered) {
                    checkEntry()
                }
            }
            if (isGradeChanged) {
                exerciseCard.base.isGradeEditedManually = true
            }
            if (isIsLearnedChanged && currentPosition == position) {
                if (currentExerciseCard.base.card.isLearned) {
                    deleteCardsForRetesting()
                    stopTimer()
                } else if (currentExerciseCard.base.isAnswerCorrect == false) {
                    updateGrade()
                    addExerciseCardToRetestIfNeed()
                }
            }
        }
        state.currentPosition = currentPosition
    }

    data class CardMoving(
        val cardId: Long,
        val deckMovedTo: Deck
    )

    sealed class Answer {
        object Show : Answer()
        object Remember : Answer()
        object NotRemember : Answer()
        class Variant(val variantIndex: Int) : Answer()
        object Entry : Answer()
    }
}