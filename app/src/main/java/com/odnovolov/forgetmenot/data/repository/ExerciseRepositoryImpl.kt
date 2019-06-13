package com.odnovolov.forgetmenot.data.repository

import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseData
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