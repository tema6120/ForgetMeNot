package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toIntervalSchemeDb
import com.odnovolov.forgetmenot.persistence.toPronunciationDb
import com.odnovolov.forgetmenot.persistence.toSpeakPlanDb

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
            ExercisePreference::testMethod -> {
                val testMethod = change.newValue as TestMethod
                queries.updateTestMethod(testMethod, exercisePreferenceId)
            }
            ExercisePreference::intervalScheme -> {
                val linkedIntervalScheme = change.newValue as IntervalScheme?
                linkedIntervalScheme?.let(::insertIntervalSchemeIfNotExists)
                queries.updateIntervalSchemeId(linkedIntervalScheme?.id, exercisePreferenceId)
            }
            ExercisePreference::pronunciation -> {
                val linkedPronunciation = change.newValue as Pronunciation
                insertPronunciationIfNotExists(linkedPronunciation)
                queries.updatePronunciationId(linkedPronunciation.id, exercisePreferenceId)
            }
            ExercisePreference::isQuestionDisplayed -> {
                val isQuestionDisplayed = change.newValue as Boolean
                queries.updateIsQuestionDisplayed(isQuestionDisplayed, exercisePreferenceId)
            }
            ExercisePreference::cardReverse -> {
                val cardReverse = change.newValue as CardReverse
                queries.updateCardReverse(cardReverse, exercisePreferenceId)
            }
            ExercisePreference::speakPlan -> {
                val linkedSpeakPlan = change.newValue as SpeakPlan
                insertSpeakPlanIfNotExists(linkedSpeakPlan)
                queries.updateSpeakPlanId(linkedSpeakPlan.id, exercisePreferenceId)
            }
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

    fun insertPronunciationIfNotExists(pronunciation: Pronunciation) {
        val exists: Boolean = pronunciation.id == Pronunciation.Default.id
                || database.pronunciationQueries.exists(pronunciation.id).executeAsOne()
        if (!exists) {
            val pronunciationDb = pronunciation.toPronunciationDb()
            database.pronunciationQueries.insert(pronunciationDb)
        }
    }

    fun insertSpeakPlanIfNotExists(speakPlan: SpeakPlan) {
        val exists: Boolean = speakPlan.isDefault()
                || database.speakPlanQueries.exists(speakPlan.id).executeAsOne()
        if (!exists) {
            val speakPlanDb = speakPlan.toSpeakPlanDb()
            database.speakPlanQueries.insert(speakPlanDb)
        }
    }
}