package com.odnovolov.forgetmenot.data.repository

import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseData
import com.odnovolov.forgetmenot.domain.repository.ExerciseRepository
import io.reactivex.Observable

class ExerciseRepositoryImpl(private val exerciseDao: ExerciseDao) : ExerciseRepository {

    override fun saveExercise(exerciseData: ExerciseData) {
        exerciseDao.insert(exerciseData)
    }
    override fun observeExercise(): Observable<ExerciseData> {
        return Observable.empty()
    }

    override fun deleteAllExercises() {
        exerciseDao.deleteAll()
    }
}