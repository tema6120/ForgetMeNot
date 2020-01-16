package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.entity.KeyGestureAction
import com.odnovolov.forgetmenot.common.entity.KeyGestureAction.*
import com.odnovolov.forgetmenot.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.screen.exercise.ExerciseOrder.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.QuizComposer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.*

class ExerciseController : BaseExerciseController<ExerciseEvent, ExerciseOrder>() {
    private val queries: ExerciseControllerQueries = database.exerciseControllerQueries
    private val textInBracketsRemover by lazy { TextInBracketsRemover() }

    init {
        launch {
            queries.answerAutoSpeakTriggered()
                .asFlow()
                .mapToOne()
                .distinctUntilChanged()
                .filter { isTriggered -> isTriggered }
                .collect {
                    dispatchSafely(AnswerAutoSpeakTriggered)
                }
        }
    }

    override fun handleEvent(event: ExerciseEvent) {
        when (event) {
            is NewPageBecameSelected -> {
                queries.setCurrentExerciseCardIdByPosition(event.position.toLong())
                if (queries.isNeedToAutoSpeakQuestion().executeAsOne()) {
                    speakQuestion()
                }
            }

            NotAskButtonClicked -> {
                setLearnedForCurrentCard()
            }

            UndoButtonClicked -> {
                queries.setLearnedForCurrentCard(false)
                queries.setVisibleToRepeatedCards(true)
            }

            SpeakButtonClicked -> {
                queries.speakingData()
                    .executeAsOne()
                    .run { speak(text, language, doNotSpeakTextInBrackets) }
            }

            EditCardButtonClicked -> {
                queries.cleanEditCardState()
                queries.initEditCardState()
                issueOrder(NavigateToEditCard)
            }

            HintButtonClicked -> {
                val (hint: String?, answer: String, startIndex: Int, endIndex: Int)
                        = queries.getHintInfo().executeAsOne() as GetHintInfo.Impl
                if (hint == null) {
                    issueOrder(ShowChooseHintPopup)
                } else {
                    val hasSelection = endIndex - startIndex > 0
                    val newHint: String = if (hasSelection) {
                        Prompter.unmaskRange(answer, hint, startIndex, endIndex)
                    } else {
                        Prompter.unmaskFirst(answer, hint)
                    }
                    queries.setHintForCurrentExerciseCard(newHint)
                }
            }

            HintAsQuizButtonClicked -> {
                queries.setQuizTestMethodForCurrentExerciseCard()
                QuizComposer.composeWhereItNeeds()
            }

            HintMaskLettersButtonClicked -> {
                val answer: String = queries.getAnswerForCurrentExerciseCard().executeAsOne()
                val hint: String = Prompter.maskLetters(answer)
                queries.setHintForCurrentExerciseCard(hint)
            }

            AnswerAutoSpeakTriggered -> {
                queries.flushAnswerAutoSpeakTriggered()
                speakAnswer()
            }

            LevelOfKnowledgeButtonClicked -> {
                if (queries.areIntervalsOn().executeAsOne()) {
                    val intervalItems: List<IntervalItem> = queries.intervalItem().executeAsList()
                    issueOrder(ShowLevelOfKnowledgePopup(intervalItems))
                } else {
                    issueOrder(ShowIntervalsAreOffMessage)
                }
            }

            is LevelOfKnowledgeSelected -> {
                queries.setLevelOfKnowledge(event.levelOfKnowledge)
                queries.setIsLevelOfKnowledgeEditedByUserTrue()
            }

            is KeyGestureDetected -> {
                val keyGestureAction: KeyGestureAction = queries
                    .getKeyGestureAction(event.keyGesture)
                    .executeAsOne()
                when (keyGestureAction) {
                    NO_ACTION -> return
                    MOVE_TO_NEXT_CARD -> issueOrder(MoveToNextPosition)
                    MOVE_TO_PREVIOUS_CARD -> issueOrder(MoveToPreviousPosition)
                    SET_CARD_AS_REMEMBER -> setCardAsRemember()
                    SET_CARD_AS_NOT_REMEMBER -> setCardAsNotRemember()
                    SET_CARD_AS_LEARNED -> setLearnedForCurrentCard()
                    SPEAK_QUESTION -> speakQuestion()
                    SPEAK_ANSWER -> speakAnswer()
                }
            }
        }
    }

    private fun speakAnswer() {
        queries.speakingDataForAnswerAutoSpeak()
            .executeAsOne()
            .run { speak(text, language, doNotSpeakTextInBrackets) }
    }

    private fun speakQuestion() {
        queries.speakingDataForQuestionAutoSpeak()
            .executeAsOne()
            .run { speak(text, language, doNotSpeakTextInBrackets) }
    }

    private fun speak(text: String, language: Locale?, doNotSpeakTextInBrackets: Boolean) {
        val textToSpeak = if (doNotSpeakTextInBrackets) {
            textInBracketsRemover.process(text)
        } else {
            text
        }
        issueOrder(Speak(textToSpeak, language))
    }

    private fun setLearnedForCurrentCard() {
        queries.setLearnedForCurrentCard(true)
        queries.setVisibleToRepeatedCards(false)
        issueOrder(MoveToNextPosition)
    }

    private fun setCardAsRemember() {
        val id = queries.getCurrentExerciseCardId().executeAsOne().currentExerciseCardId ?: return
        val isAnswered = queries.isAnswered().executeAsOne()
        onCorrectAnswer(id)
        if (isAnswered) {
            speakAnswer()
        }
    }

    private fun setCardAsNotRemember() {
        val id = queries.getCurrentExerciseCardId().executeAsOne().currentExerciseCardId ?: return
        val isAnswered = queries.isAnswered().executeAsOne()
        onWrongAnswer(id)
        if (isAnswered) {
            speakAnswer()
        }
    }
}