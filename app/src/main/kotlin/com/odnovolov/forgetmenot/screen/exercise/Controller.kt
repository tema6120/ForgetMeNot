package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.screen.exercise.ExerciseOrder.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.QuizComposer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.*

class ExerciseController : BaseController<ExerciseEvent, ExerciseOrder>() {
    private val queries: ExerciseControllerQueries = database.exerciseControllerQueries
    private val textInBracketsRemover by lazy { TextInBracketsRemover() }

    init {
        launch {
            queries.answerAutoSpeakTriggered()
                .asFlow()
                .mapToOne()
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
                if (queries.isNeedToAutoSpeak().executeAsOne()) {
                    queries.speakingDataForQuestionAutoSpeak()
                        .executeAsOne()
                        .run { speak(text, language, doNotSpeakTextInBrackets) }
                }
            }

            NotAskButtonClicked -> {
                queries.setLearnedForCurrentCard(true)
                queries.setVisibleToRepeatedCards(false)
                issueOrder(MoveToNextPosition)
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
                val hintAndAnswer = queries.getHintAndAnswerForCurrentExerciseCard().executeAsOne()
                val hint: String? = hintAndAnswer.hint
                if (hint == null) {
                    issueOrder(ShowChooseHintPopup)
                } else {
                    val answer: String = hintAndAnswer.answer
                    val newHint: String = Prompter.unmaskFirst(answer, hint)
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
                queries.speakingDataForAnswerAutoSpeak()
                    .executeAsOne()
                    .run { speak(text, language, doNotSpeakTextInBrackets) }
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
        }
    }

    private fun speak(text: String, language: Locale?, doNotSpeakTextInBrackets: Boolean) {
        val textToSpeak = if (doNotSpeakTextInBrackets) {
            textInBracketsRemover.process(text)
        } else {
            text
        }
        issueOrder(Speak(textToSpeak, language))
    }
}