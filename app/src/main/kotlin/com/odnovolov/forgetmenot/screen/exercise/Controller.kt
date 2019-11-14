package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.exercise.ExerciseControllerQueries
import com.odnovolov.forgetmenot.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.screen.exercise.ExerciseOrder.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class ExerciseController : BaseController<ExerciseEvent, ExerciseOrder>() {
    private val queries: ExerciseControllerQueries = database.exerciseControllerQueries

    init {
        launch {
            queries.answerAutoSpeakTriggered()
                .asFlow()
                .mapToOne()
                .filter { it }
                .collect {
                    dispatchSafely(AnswerAutoSpeakTriggered)
                }
        }
    }

    override fun handleEvent(event: ExerciseEvent) {
        when (event) {
            is NewPageBecameSelected -> {
                val exerciseCardIds = queries.getAllExerciseCardIds().executeAsList()
                val currentExerciseCardId = exerciseCardIds[event.position]
                queries.setCurrentExerciseCardId(currentExerciseCardId)
                if (queries.isQuestionAutoSpeakEnabled().executeAsOne()) {
                    val textToSpeakAndLanguage = queries.getQuestionAndLanguageToSpeak()
                        .executeAsOne()
                    issueOrder(
                        Speak(
                            text = textToSpeakAndLanguage.textToSpeak,
                            language = textToSpeakAndLanguage.language
                        )
                    )
                }
            }

            NotAskButtonClicked -> {
                queries.setLearnedForCurrentCard(true)
                issueOrder(MoveToNextPosition)
            }

            UndoButtonClicked -> {
                queries.setLearnedForCurrentCard(false)
            }

            SpeakButtonClicked -> {
                val textToSpeakAndLanguage = queries.getTextToSpeakAndLanguage().executeAsOne()
                issueOrder(
                    Speak(
                        text = textToSpeakAndLanguage.textToSpeak,
                        language = textToSpeakAndLanguage.language
                    )
                )
            }

            EditCardButtonClicked -> {
                with(database.editCardInitQueries) {
                    createTableEditCardState()
                    cleanTableEditCardState()
                    initTableEditCardState()
                }
                issueOrder(NavigateToEditCard)
            }

            AnswerAutoSpeakTriggered -> {
                queries.flushAnswerAutoSpeakTriggered()
                val textToSpeakAndLanguage = queries.getAnswerAndLanguageToSpeak().executeAsOne()
                issueOrder(
                    Speak(
                        text = textToSpeakAndLanguage.textToSpeak,
                        language = textToSpeakAndLanguage.language
                    )
                )
            }
        }
    }
}