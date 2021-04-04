package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toGradingDb
import com.odnovolov.forgetmenot.persistence.toIntervalSchemeDb
import com.odnovolov.forgetmenot.persistence.toPronunciationDb
import com.odnovolov.forgetmenot.persistence.toPronunciationPlanDb

class ExercisePreferencePropertyChangeHandler(
    private val database: Database,
    private val intervalSchemePropertyChangeHandler: IntervalSchemePropertyChangeHandler
) : PropertyChangeHandler {
    private val queries = database.exercisePreferenceQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val exercisePreferenceId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(exercisePreferenceId).executeAsOne()
        if (!exists) return
        when (change.property) {
            ExercisePreference::name -> {
                val name = change.newValue as String
                queries.updateName(name, exercisePreferenceId)
            }
            ExercisePreference::randomOrder -> {
                val randomOrder = change.newValue as Boolean
                queries.updateRandomOrder(randomOrder, exercisePreferenceId)
            }
            ExercisePreference::pronunciation -> {
                val linkedPronunciation = change.newValue as Pronunciation
                insertPronunciationIfNotExists(linkedPronunciation)
                queries.updatePronunciationId(linkedPronunciation.id, exercisePreferenceId)
            }
            ExercisePreference::cardInversion -> {
                val cardInversion = change.newValue as CardInversion
                queries.updateCardInversion(cardInversion, exercisePreferenceId)
            }
            ExercisePreference::isQuestionDisplayed -> {
                val isQuestionDisplayed = change.newValue as Boolean
                queries.updateIsQuestionDisplayed(isQuestionDisplayed, exercisePreferenceId)
            }
            ExercisePreference::testingMethod -> {
                val testingMethod = change.newValue as TestingMethod
                queries.updateTestingMethod(testingMethod, exercisePreferenceId)
            }
            ExercisePreference::grading -> {
                val linkedGrading = change.newValue as Grading
                insertGradingIfNotExists(linkedGrading)
                queries.updateGradingId(linkedGrading.id, exercisePreferenceId)
            }
            ExercisePreference::intervalScheme -> {
                val linkedIntervalScheme = change.newValue as IntervalScheme?
                linkedIntervalScheme?.let(::insertIntervalSchemeIfNotExists)
                queries.updateIntervalSchemeId(linkedIntervalScheme?.id, exercisePreferenceId)
            }
            ExercisePreference::pronunciationPlan -> {
                val linkedPronunciationPlan = change.newValue as PronunciationPlan
                insertPronunciationPlanIfNotExists(linkedPronunciationPlan)
                queries.updatePronunciationPlanId(linkedPronunciationPlan.id, exercisePreferenceId)
            }
            ExercisePreference::timeForAnswer -> {
                val timeForAnswer = change.newValue as Int
                queries.updateTimeForAnswer(timeForAnswer, exercisePreferenceId)
            }
        }
    }

    fun insertPronunciationIfNotExists(pronunciation: Pronunciation) {
        val exists: Boolean = pronunciation.id == Pronunciation.Default.id
                || database.pronunciationQueries.exists(pronunciation.id).executeAsOne()
        if (!exists) {
            val pronunciationDb = pronunciation.toPronunciationDb()
            database.pronunciationQueries.insert(pronunciationDb)
        }
    }

    fun insertIntervalSchemeIfNotExists(intervalScheme: IntervalScheme) {
        val exists = intervalScheme.id == IntervalScheme.Default.id
                || database.intervalSchemeQueries.exists(intervalScheme.id).executeAsOne()
        if (!exists) {
            val intervalSchemeDb = intervalScheme.toIntervalSchemeDb()
            database.intervalSchemeQueries.insert(intervalSchemeDb)
            intervalSchemePropertyChangeHandler.insertIntervals(
                intervalScheme.intervals,
                intervalScheme.id
            )
        }
    }

    fun insertGradingIfNotExists(grading: Grading) {
        val exists: Boolean = grading.id == Grading.Default.id
                || database.gradingQueries.exists(grading.id).executeAsOne()
        if (!exists) {
            val gradingDb = grading.toGradingDb()
            database.gradingQueries.insert(gradingDb)
        }
    }

    fun insertPronunciationPlanIfNotExists(pronunciationPlan: PronunciationPlan) {
        val exists: Boolean = pronunciationPlan.isDefault()
                || database.pronunciationPlanQueries.exists(pronunciationPlan.id).executeAsOne()
        if (!exists) {
            val pronunciationPlanDb = pronunciationPlan.toPronunciationPlanDb()
            database.pronunciationPlanQueries.insert(pronunciationPlanDb)
        }
    }
}