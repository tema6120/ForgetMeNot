package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnCorrectAnswer
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnWrongAnswer
import com.odnovolov.forgetmenot.domain.entity.Grading
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler

class GradingPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.gradingQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val gradingId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(gradingId).executeAsOne()
        if (!exists) return
        when (change.property) {
            Grading::onFirstCorrectAnswer -> {
                val onFirstCorrectAnswer = change.newValue as GradeChangeOnCorrectAnswer
                queries.updateOnFirstCorrectAnswer(onFirstCorrectAnswer, gradingId)
            }
            Grading::onFirstWrongAnswer -> {
                val onFirstWrongAnswer = change.newValue as GradeChangeOnWrongAnswer
                queries.updateOnFirstWrongAnswer(onFirstWrongAnswer, gradingId)
            }
            Grading::askAgain -> {
                val askAgain = change.newValue as Boolean
                queries.updateAskAgain(askAgain, gradingId)
            }
            Grading::onRepeatedCorrectAnswer -> {
                val onRepeatedCorrectAnswer = change.newValue as GradeChangeOnCorrectAnswer
                queries.updateOnRepeatedCorrectAnswer(onRepeatedCorrectAnswer, gradingId)
            }
            Grading::onRepeatedWrongAnswer -> {
                val onRepeatedWrongAnswer = change.newValue as GradeChangeOnWrongAnswer
                queries.updateOnRepeatedWrongAnswer(onRepeatedWrongAnswer, gradingId)
            }
        }
    }
}