package com.odnovolov.forgetmenot.decksettings

import com.odnovolov.forgetmenot.common.database.ExercisePreference
import com.odnovolov.forgetmenot.common.database.database

object ExercisePreferenceUpdater {
    private val queries = database.exercisePreferenceUpdaterQueries

    fun updateCurrentExercisePreference(
        makeNewExercisePreferenceOutOfOld:
            (oldExercisePreference: ExercisePreference.Impl) -> ExercisePreference.Impl
    ) {
        val oldExercisePreference = queries.getCurrentExercisePreference().executeAsOne()
                as ExercisePreference.Impl
        val wishfulExercisePreference = makeNewExercisePreferenceOutOfOld(oldExercisePreference)
        updateCurrentExercisePreference(wishfulExercisePreference)
    }

    fun updateCurrentExercisePreference(wishfulExercisePreference: ExercisePreference.Impl) {
        val newExercisePreferenceId =
            if (shouldExercisePreferenceBeDefault(wishfulExercisePreference)) {
                0L
            } else {
                val exercisePreferenceIdToChange =
                    findExercisePreferenceIdToUpdate(wishfulExercisePreference)
                if (exercisePreferenceIdToChange != null) {
                    with(wishfulExercisePreference) {
                        queries.changeExercisePreference(
                            name, randomOrder, pronunciationId,
                            where = exercisePreferenceIdToChange
                        )
                    }
                    exercisePreferenceIdToChange
                } else {
                    with(wishfulExercisePreference) {
                        queries.addNewExercisePreference(name, randomOrder, pronunciationId)
                    }
                    queries.getLastInsertId().executeAsOne()
                }
            }
        val currentExercisePreferenceId = queries.getCurrentExercisePreferenceId().executeAsOne()
        if (currentExercisePreferenceId != newExercisePreferenceId) {
            queries.bindExercisePreferenceToDeck(newExercisePreferenceId)
            queries.deleteUnusedIndividualExercisePreference()
        }
    }

    private fun shouldExercisePreferenceBeDefault(
        sourceExercisePreference: ExercisePreference.Impl
    ): Boolean {
        val defaultExercisePreference = queries.getDefaultExercisePreference().executeAsOne()
                as ExercisePreference.Impl
        return defaultExercisePreference ==
                sourceExercisePreference.copy(id = defaultExercisePreference.id)
    }

    private fun findExercisePreferenceIdToUpdate(
        sourceExercisePreference: ExercisePreference
    ): Long? {
        return if (sourceExercisePreference.name.isNotEmpty()) {
            queries.getExercisePreferenceIdByName(sourceExercisePreference.name)
                .executeAsOneOrNull()
        } else {
            queries.getCurrentExercisePreferenceIdIfItIsIndividual().executeAsOneOrNull()
        }
    }
}