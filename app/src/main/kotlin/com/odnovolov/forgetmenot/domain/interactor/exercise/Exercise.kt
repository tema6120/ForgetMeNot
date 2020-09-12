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

    val currentExerciseCard: ExerciseCard get() = state.exerciseCards[state.currentPosition]
    private val isWalkingMode get() = globalState.isWalkingModeEnabled
    private lateinit var currentPronunciation: Pronunciation
    private val textInBracketsRemover by lazy { TextInBracketsRemover() }
    private var timerJob: Job? = null

    init {
        updateCurrentPronunciation()
        autoSpeakQuestionIfNeed()
        updateLastOpenedAt()
        startTimer()
    }

    fun setCurrentPosition(position: Int) {
        if (position >= state.exerciseCards.size || position == state.currentPosition) {
            return
        }
        resetTimer()
        state.currentPosition = position
        updateCurrentPronunciation()
        autoSpeakQuestionIfNeed()
        updateLastOpenedAt()
        startTimer()
    }

    private fun updateCurrentPronunciation() {
        val associatedPronunciation = currentExerciseCard.base.deck.exercisePreference.pronunciation
        currentPronunciation = if (currentExerciseCard.base.isReverse) {
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

    private fun updateLastOpenedAt() {
        currentExerciseCard.base.deck.lastOpenedAt = DateTime.now()
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

    fun setIsCardLearned(isLearned: Boolean) {
        if (currentExerciseCard.base.card.isLearned == isLearned) return
        currentExerciseCard.base.card.isLearned = isLearned
        if (isLearned) {
            deleteCardsForRetesting()
            resetTimer()
        } else {
            if (currentExerciseCard.base.isAnswerCorrect == false) {
                updateLevelOfKnowledge()
                addExerciseCardToRetestIfNeed()
            }
            startTimer()
        }
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
            currentPronunciation.questionLanguage
        )
    }

    private fun speakAnswerSelection() {
        speak(
            state.answerSelection,
            currentPronunciation.answerLanguage
        )
    }

    private fun autoSpeakQuestionIfNeed() {
        if (isWalkingMode
            || currentPronunciation.questionAutoSpeak
            && !currentExerciseCard.base.card.isLearned
            && !currentExerciseCard.isAnswered
        ) {
            speakQuestion()
        }
    }

    private fun autoSpeakAnswerIfNeed() {
        if (isWalkingMode
            || currentPronunciation.answerAutoSpeak
            && !currentExerciseCard.base.card.isLearned
            && !currentExerciseCard.isAnswered
        ) {
            speakAnswer()
        }
    }

    fun speakQuestion() {
        with(currentExerciseCard.base) {
            val question = if (isReverse) card.answer else card.question
            speak(question, currentPronunciation.questionLanguage)
        }
    }

    fun speakAnswer() {
        with(currentExerciseCard.base) {
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
        speaker.stop()
    }

    fun notifyCardChanged(
        card: Card,
        isQuestionChanged: Boolean,
        isAnswerChanged: Boolean,
        isLevelOfKnowledgeChanged: Boolean,
        isIsLearnedChanged: Boolean
    ) {
        val currentPosition = state.currentPosition
        state.exerciseCards.forEachIndexed { position, exerciseCard ->
            if (card.id != exerciseCard.base.card.id) return@forEachIndexed
            state.currentPosition = position
            val isReverse = exerciseCard.base.isReverse
            val isActualAnswerChanged: Boolean =
                isReverse && isQuestionChanged || !isReverse && isAnswerChanged
            if (isActualAnswerChanged) {
                exerciseCard.base.hint = null
                if (exerciseCard is EntryTestExerciseCard && exerciseCard.isAnswered) {
                    checkEntry()
                }
            }
            if (isLevelOfKnowledgeChanged) {
                exerciseCard.base.isLevelOfKnowledgeEditedManually = true
            }
            if (isIsLearnedChanged && currentPosition == position) {
                if (currentExerciseCard.base.card.isLearned) {
                    deleteCardsForRetesting()
                    resetTimer()
                } else if (currentExerciseCard.base.isAnswerCorrect == false) {
                    updateLevelOfKnowledge()
                    addExerciseCardToRetestIfNeed()
                }
            }
        }
        state.currentPosition = currentPosition
    }

    fun setWalkingModeEnabled(enabled: Boolean) {
        if (isWalkingMode == enabled) return
        stopTimer()
        globalState.isWalkingModeEnabled = enabled
        conformExerciseCards()
        startTimer()
    }

    private fun conformExerciseCards() {
        var hasChange = false
        val conformedExerciseCards = state.exerciseCards
            .map { exerciseCard ->
                when {
                    exerciseCard.isAnswered -> exerciseCard
                    exerciseCard.shouldMapToManualTestExerciseCard() -> {
                        hasChange = true
                        ManualTestExerciseCard(exerciseCard.base)
                    }
                    exerciseCard.shouldMapToQuizTestExerciseCard() -> {
                        hasChange = true
                        val variants: List<Card?> = with(exerciseCard.base) {
                            QuizComposer.compose(card, deck, isReverse, withCaching = true)
                        }
                        QuizTestExerciseCard(exerciseCard.base, variants)
                    }
                    exerciseCard.shouldMapToEntryTestExerciseCard() -> {
                        hasChange = true
                        EntryTestExerciseCard(exerciseCard.base)
                    }
                    else -> exerciseCard
                }.apply {
                    if (isWalkingMode
                        && base.timeLeft != 0
                    ) {
                        base.timeLeft = 0
                    } else if (!isWalkingMode
                        && !base.card.isLearned
                        && !isAnswered
                        && !base.isExpired
                    ) {
                        base.timeLeft = base.deck.exercisePreference.timeForAnswer
                    }
                }
            }
        QuizComposer.clearCache()
        if (hasChange) {
            state.exerciseCards = conformedExerciseCards
        }
    }

    private fun ExerciseCard.shouldMapToManualTestExerciseCard(): Boolean =
        isWalkingMode && (this is QuizTestExerciseCard || this is EntryTestExerciseCard)

    private fun ExerciseCard.shouldMapToQuizTestExerciseCard(): Boolean =
        !isWalkingMode
                && base.deck.exercisePreference.testMethod == TestMethod.Quiz
                && this !is QuizTestExerciseCard

    private fun ExerciseCard.shouldMapToEntryTestExerciseCard(): Boolean =
        !isWalkingMode
                && base.deck.exercisePreference.testMethod == TestMethod.Entry
                && this !is EntryTestExerciseCard

    fun setLevelOfKnowledge(levelOfKnowledge: Int) {
        if (levelOfKnowledge < 0) return
        currentExerciseCard.base.card.levelOfKnowledge = levelOfKnowledge
        state.exerciseCards.filter { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.id == currentExerciseCard.base.card.id
        }
            .forEach { exerciseCard: ExerciseCard ->
                exerciseCard.base.isLevelOfKnowledgeEditedManually = true
            }
    }

    fun setHintSelection(startIndex: Int, endIndex: Int) {
        with(state.hintSelection) {
            this.startIndex = startIndex
            this.endIndex = endIndex
        }
    }

    fun showHint() {
        fun hasHint() = currentExerciseCard.base.hint != null
        fun hasHintSelection() = state.hintSelection.endIndex - state.hintSelection.startIndex > 0
        val answer: String = with(currentExerciseCard.base) {
            if (isReverse) card.question else card.answer
        }
        val oldHint: String? = currentExerciseCard.base.hint
        currentExerciseCard.base.hint = when {
            !hasHint() -> Prompter.maskLetters(answer)
            hasHintSelection() ->
                Prompter.unmaskRange(
                    answer,
                    oldHint!!,
                    state.hintSelection.startIndex,
                    state.hintSelection.endIndex
                )
            else -> Prompter.unmaskFirst(answer, oldHint!!)
        }
    }

    fun hintAsQuiz() {
        if (isWalkingMode
            || currentExerciseCard is QuizTestExerciseCard
            || currentExerciseCard.base.card.isLearned
            || currentExerciseCard.isAnswered
        ) {
            return
        }
        resetTimer()
        val variants: List<Card?> = with(currentExerciseCard.base) {
            QuizComposer.compose(card, deck, isReverse, withCaching = false)
        }
        val newQuizTestExerciseCard = QuizTestExerciseCard(currentExerciseCard.base, variants)
        state.exerciseCards = state.exerciseCards.toMutableList().run {
            this[state.currentPosition] = newQuizTestExerciseCard
            toList()
        }
        startTimer()
    }

    fun startTimer() {
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
        with(currentExerciseCard.base) {
            if (isWalkingMode
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
        if (isVariantCorrect) setAnswerAsCorrect()
        else setAnswerAsWrong()
    }

    private fun checkEntry() {
        val entryExerciseCard = currentExerciseCard as EntryTestExerciseCard
        val correctAnswer = with(entryExerciseCard.base) {
            if (isReverse) card.question else card.answer
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
        updateLevelOfKnowledge()
        updateLastAnsweredAt()
    }

    private fun setAnswerAsWrong() {
        if (currentExerciseCard.base.isAnswerCorrect == false) return
        autoSpeakAnswerIfNeed()
        incrementLapIfCardIsAnsweredForTheFirstTime()
        currentExerciseCard.base.isAnswerCorrect = false
        showQuestion()
        addExerciseCardToRetestIfNeed()
        updateLevelOfKnowledge()
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
        if (hasExerciseCardForRetesting()) return
        val baseExerciseCard = with(currentExerciseCard.base) {
            ExerciseCard.Base(
                id = generateId(),
                card = card,
                deck = deck,
                isReverse = isReverse,
                isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
                timeLeft = if (isWalkingMode) 0 else deck.exercisePreference.timeForAnswer,
                initialLevelOfKnowledge = initialLevelOfKnowledge,
                isLevelOfKnowledgeEditedManually = isLevelOfKnowledgeEditedManually
            )
        }
        val retestingExerciseCard: ExerciseCard =
            when (currentExerciseCard.base.deck.exercisePreference.testMethod) {
                TestMethod.Off -> OffTestExerciseCard(baseExerciseCard)
                TestMethod.Manual -> ManualTestExerciseCard(baseExerciseCard)
                TestMethod.Quiz -> {
                    if (isWalkingMode) {
                        ManualTestExerciseCard(baseExerciseCard)
                    } else {
                        val variants: List<Card?> = with(baseExerciseCard) {
                            QuizComposer.compose(card, deck, isReverse, withCaching = false)
                        }
                        QuizTestExerciseCard(baseExerciseCard, variants)
                    }
                }
                TestMethod.Entry -> {
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

    private fun updateLevelOfKnowledge() {
        if (currentExerciseCard.base.isLevelOfKnowledgeEditedManually) return
        var numberOfCorrect = 0
        var numberOfWrong = 0
        for (exerciseCard in state.exerciseCards) {
            if (exerciseCard.base.card.id != currentExerciseCard.base.card.id) continue
            when (exerciseCard.base.isAnswerCorrect) {
                true -> numberOfCorrect++
                false -> numberOfWrong++
            }
        }
        with(currentExerciseCard.base) {
            card.levelOfKnowledge = when {
                numberOfWrong > 0 -> maxOf(0, initialLevelOfKnowledge - numberOfWrong)
                numberOfCorrect > 0 -> initialLevelOfKnowledge + 1
                else -> return
            }
        }
    }

    private fun updateLastAnsweredAt() {
        currentExerciseCard.base.card.lastAnsweredAt = DateTime.now()
    }

    sealed class Answer {
        object Show : Answer()
        object Remember : Answer()
        object NotRemember : Answer()
        class Variant(val variantIndex: Int) : Answer()
        object Entry : Answer()
    }
}