package com.odnovolov.forgetmenot.exercise

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.exercise.ExerciseOrder.*

class ExerciseController : BaseController<ExerciseEvent, ExerciseOrder>() {
    private val queries: ExerciseControllerQueries = database.exerciseControllerQueries

    override fun handleEvent(event: ExerciseEvent) {
        when (event) {
            is NewPageBecameSelected -> {
                val exerciseCardIds = queries.getAllExerciseCardIds().executeAsList()
                val currentExerciseCardId = exerciseCardIds[event.position]
                queries.setCurrentExerciseCardId(currentExerciseCardId)
            }

            NotAskButtonClicked -> {
                queries.setLearnedForCurrentCard(true)
                issueOrder(MoveToNextPosition)
            }

            UndoButtonClicked -> {
                queries.setLearnedForCurrentCard(false)
            }

            SpeakButtonClicked -> {
                val isAnswered: Long = queries.isCurrentExerciseCardAnswered().executeAsOne()
                val textToSpeakAndLanguage = queries
                    .getTextToSpeakAndLanguage(isAnswered)
                    .executeAsOne()
                issueOrder(
                    Speak(
                        text = textToSpeakAndLanguage.textToSpeak,
                        language = textToSpeakAndLanguage.language
                    )
                )
            }

            EditCardButtonClicked -> {
                with(database.editCardStateQueries) {
                    dropTableEditCardState()
                    createTableEditCardState()
                    initTableEditCardState()
                }
                issueOrder(NavigateToEditCard)
            }
        }
    }
}