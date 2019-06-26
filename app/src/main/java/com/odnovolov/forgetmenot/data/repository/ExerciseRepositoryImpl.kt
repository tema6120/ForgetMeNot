package com.odnovolov.forgetmenot.data.repository

import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.domain.entity.ExerciseCard
import com.odnovolov.forgetmenot.domain.entity.ExerciseData
import com.odnovolov.forgetmenot.domain.repository.ExerciseRepository
import io.reactivex.Observable

class ExerciseRepositoryImpl(private val exerciseDao: ExerciseDao) : ExerciseRepository {

    override fun saveExercise(exerciseData: ExerciseData) {
        exerciseDao.insert(exerciseData)
    }

    override fun observeExercise(): Observable<ExerciseData> {
        return exerciseDao.observeExercise()
    }

    override fun updateExerciseCard(exerciseCard: ExerciseCard) {
        exerciseDao.updateExerciseCard(exerciseCard)
    }

    override fun deleteAllExercises() {
        exerciseDao.deleteAll()
    }
}