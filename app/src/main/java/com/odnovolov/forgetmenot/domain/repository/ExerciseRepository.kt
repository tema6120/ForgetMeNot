package com.odnovolov.forgetmenot.domain.repository

import com.odnovolov.forgetmenot.domain.feature.exercise.Exercise

interface ExerciseRepository {
    // Create
    fun saveExercise(exercise: Exercise)

    // Delete
    fun deleteAllExercises()
}