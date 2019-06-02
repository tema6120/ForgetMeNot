package com.odnovolov.forgetmenot.data.repository

import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.domain.feature.exercise.Exercise
import com.odnovolov.forgetmenot.domain.repository.ExerciseRepository

class ExerciseRepositoryImpl(private val exerciseDao: ExerciseDao) : ExerciseRepository {
    override fun saveExercise(exercise: Exercise) {
        exerciseDao.insert(exercise)
    }

    override fun deleteAllExercises() {
        exerciseDao.deleteAll()
    }
}