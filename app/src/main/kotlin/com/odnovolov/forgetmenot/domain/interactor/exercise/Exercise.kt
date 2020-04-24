package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.*
import com.soywiz.klock.DateTime
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class Exercise(
    val state: State,
    private val speaker: Speaker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    class State(
        exerciseCards: List<ExerciseCard>,
        currentPosition: Int = 0,
        questionSelection: String = "",
        answerSelection: String = "",
        hintSelection: HintSelection = HintSelection(0, 0),
        isWalkingMode: Boolean
    ) : FlowableState<State>() {
        var exerciseCards: List<ExerciseCard> by me(exerciseCards)
        var currentPosition: Int by me(currentPosition)
        var questionSelection: String by me(questionSelection)
        var answerSelection: String by me(answerSelection)
        var hintSelection: HintSelection by me(hintSelection)
        val isWalkingMode: Boolean by me(isWalkingMode)
    }

    val currentExerciseCard: ExerciseCard get() = state.exerciseCards[state.currentPosition]
    private lateinit var currentPronunciation: Pronunciation
    private val textInBracketsRemover by lazy { TextInBracketsRemover() }
    private var timerJob: Job? = null

    init {
        updateLastOpenedAt()
        updateCurrentPronunciation()
        autoSpeakQuestionIfNeed()
        startTimer()
    }

    private fun updateLastOpenedAt() {
        val now = DateTime.now()
        state.exerciseCards
            .map { it.base.deck }
            .distinctBy { it.id }
            .forEach { deck -> deck.lastOpenedAt = now }
    }

    fun setCurrentPosition(position: Int) {
        if (position >= state.exerciseCards.size || position == state.currentPosition) {
            return
        }
        resetTimer()
        state.currentPosition = position
        updateCurrentPronunciation()
        autoSpeakQuestionIfNeed()
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
        currentExerciseCard.base.card.isLearned = isLearned
        if (isLearned) resetTimer() else startTimer()
    }

    fun speak() {
        when {
            hasQuestionSelection() -> speakQuestionSelection()
            hasAnswerSelection() -> speakAnswerSelection()
            isAnswered() -> speakAnswer()
            else -> speakQuestion()
        }
    }

    private fun hasAnswerSelection(): Boolean = state.answerSelection.isNotEmpty()
    private fun hasQuestionSelection(): Boolean = state.questionSelection.isNotEmpty()
    private fun isAnswered(): Boolean = currentExerciseCard.base.isAnswerCorrect != null

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
        if (state.isWalkingMode
            || currentPronunciation.questionAutoSpeak
            && !currentExerciseCard.base.card.isLearned
            && currentExerciseCard.base.isAnswerCorrect == null
        ) {
            speakQuestion()
        }
    }

    private fun autoSpeakAnswerIfNeed() {
        if (state.isWalkingMode
            || currentPronunciation.answerAutoSpeak
            && !currentExerciseCard.base.card.isLearned
            && currentExerciseCard.base.isAnswerCorrect == null
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

    fun notifyCurrentCardChanged() {
        val exerciseCard = currentExerciseCard
        exerciseCard.base.hint = null
        if (exerciseCard is EntryTestExerciseCard && exerciseCard.base.isAnswerCorrect != null) {
            checkEntry(exerciseCard.userAnswer)
        }
    }

    fun setLevelOfKnowledge(levelOfKnowledge: Int) {
        if (levelOfKnowledge < 0) return
        with(currentExerciseCard.base) {
            card.levelOfKnowledge = levelOfKnowledge
            isLevelOfKnowledgeEditedManually = true
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
        if (state.isWalkingMode
            || currentExerciseCard is QuizTestExerciseCard
            || currentExerciseCard.base.card.isLearned
            || isAnswered()
        ) {
            return
        }
        val baseExerciseCard = with(currentExerciseCard.base) {
            ExerciseCard.Base(
                id = id,
                card = card,
                deck = deck,
                isReverse = isReverse,
                isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
                timeLeft = timeLeft,
                initialLevelOfKnowledge = initialLevelOfKnowledge,
                isLevelOfKnowledgeEditedManually = isLevelOfKnowledgeEditedManually
            )
        }
        val variants: List<Card?> = with(baseExerciseCard) {
            QuizComposer.compose(card, deck, isReverse, withCaching = false)
        }
        val newQuizTestExerciseCard = QuizTestExerciseCard(baseExerciseCard, variants)
        stopTimer()
        state.exerciseCards = state.exerciseCards.toMutableList().run {
            this[state.currentPosition] = newQuizTestExerciseCard
            toList()
        }
        startTimer()
    }

    fun startTimer() {
        with(currentExerciseCard.base) {
            if (state.isWalkingMode
                || card.isLearned
                || isAnswered()
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
            if (state.isWalkingMode
                || isExpired
                || isAnswered()
            ) {
                return
            }
            timerJob?.cancel()
            timeLeft = deck.exercisePreference.timeForAnswer
        }
    }

    fun stopTimer() {
        with(currentExerciseCard.base) {
            if (state.isWalkingMode
                || isExpired
                || isAnswered()
            ) {
                return
            }
            timerJob?.cancel()
            timeLeft = 0
        }
    }

    fun answer(answer: Answer) {
        if (!isAnswerRelevant(answer)) return
        stopTimer()
        when (answer) {
            Show, Remember -> setAnswerAsCorrect()
            NotRemember -> setAnswerAsWrong()
            is Variant -> checkVariant(answer.variantIndex)
            is Entry -> checkEntry(answer.userAnswer)
        }
    }

    private fun isAnswerRelevant(answer: Answer): Boolean {
        return when (answer) {
            Show, Remember, NotRemember -> currentExerciseCard is OffTestExerciseCard
                    || currentExerciseCard is ManualTestExerciseCard
            is Variant -> currentExerciseCard is QuizTestExerciseCard
            is Entry -> currentExerciseCard is EntryTestExerciseCard
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

    private fun checkEntry(userAnswer: String?) {
        val entryExerciseCard = currentExerciseCard as EntryTestExerciseCard
        entryExerciseCard.userAnswer = userAnswer
        val correctAnswer = with(entryExerciseCard.base) {
            if (isReverse) card.question else card.answer
        }
        val isUserAnswerCorrect = userAnswer?.trim() == correctAnswer.trim()
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
        updateLevelOfKnowledge()
        deleteAllRetestedCards()
        updateLastAnsweredAt()
    }

    private fun setAnswerAsWrong() {
        if (currentExerciseCard.base.isAnswerCorrect == false) return
        autoSpeakAnswerIfNeed()
        incrementLapIfCardIsAnsweredForTheFirstTime()
        currentExerciseCard.base.isAnswerCorrect = false
        showQuestion()
        updateLevelOfKnowledge()
        addExerciseCardToRetestIfNeed()
        updateLastAnsweredAt()
    }

    private fun incrementLapIfCardIsAnsweredForTheFirstTime() {
        val isAnsweredForTheFirstTime = state.exerciseCards
            .any { exerciseCard: ExerciseCard ->
                exerciseCard.base.card.id == currentExerciseCard.base.card.id
                        && exerciseCard.base.isAnswerCorrect != null
            }
            .not()
        if (isAnsweredForTheFirstTime) {
            currentExerciseCard.base.card.lap++
        }
    }

    private fun updateLevelOfKnowledge() {
        if (currentExerciseCard.base.isLevelOfKnowledgeEditedManually) return
        var numberOfCorrect = 0
        var numberOfWrong = 0
        state.exerciseCards.forEach {
            if (it.base.card.id == currentExerciseCard.base.card.id) {
                when (it.base.isAnswerCorrect) {
                    true -> numberOfCorrect++
                    false -> numberOfWrong++
                }
            }
        }
        if (numberOfWrong > 0) {
            currentExerciseCard.base.card.levelOfKnowledge =
                maxOf(0, currentExerciseCard.base.initialLevelOfKnowledge - numberOfWrong)
        } else if (numberOfCorrect > 0) {
            currentExerciseCard.base.card.levelOfKnowledge =
                currentExerciseCard.base.initialLevelOfKnowledge + 1
        }
    }

    private fun deleteAllRetestedCards() {
        if (hasExerciseCardToRetest()) {
            state.exerciseCards = state.exerciseCards
                .filterIndexed { index, exerciseCard ->
                    exerciseCard.base.card.id != currentExerciseCard.base.card.id
                            || exerciseCard.base.card.id == currentExerciseCard.base.card.id
                            && index <= state.currentPosition
                }
        }
    }

    private fun addExerciseCardToRetestIfNeed() {
        if (hasExerciseCardToRetest()) return
        val baseExerciseCard = with(currentExerciseCard.base) {
            ExerciseCard.Base(
                id = generateId(),
                card = card,
                deck = deck,
                isReverse = isReverse,
                isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
                timeLeft = deck.exercisePreference.timeForAnswer,
                initialLevelOfKnowledge = initialLevelOfKnowledge,
                isLevelOfKnowledgeEditedManually = isLevelOfKnowledgeEditedManually
            )
        }
        val retestedExerciseCard: ExerciseCard =
            when (currentExerciseCard.base.deck.exercisePreference.testMethod) {
                TestMethod.Off -> OffTestExerciseCard(baseExerciseCard)
                TestMethod.Manual -> ManualTestExerciseCard(baseExerciseCard)
                TestMethod.Quiz -> {
                    if (state.isWalkingMode) {
                        ManualTestExerciseCard(baseExerciseCard)
                    } else {
                        val variants: List<Card?> = with(baseExerciseCard) {
                            QuizComposer.compose(card, deck, isReverse, withCaching = false)
                        }
                        QuizTestExerciseCard(baseExerciseCard, variants)
                    }
                }
                TestMethod.Entry -> {
                    if (state.isWalkingMode) {
                        ManualTestExerciseCard(baseExerciseCard)
                    } else {
                        EntryTestExerciseCard(baseExerciseCard)
                    }
                }
            }
        state.exerciseCards += retestedExerciseCard
    }

    private fun hasExerciseCardToRetest(): Boolean {
        return state.exerciseCards
            .drop(state.currentPosition + 1)
            .any { it.base.card.id == currentExerciseCard.base.card.id }
    }

    private fun updateLastAnsweredAt() {
        currentExerciseCard.base.card.lastAnsweredAt = DateTime.now()
    }

    sealed class Answer {
        object Show : Answer()
        object Remember : Answer()
        object NotRemember : Answer()
        class Variant(val variantIndex: Int) : Answer()
        class Entry(val userAnswer: String?) : Answer()
    }
}