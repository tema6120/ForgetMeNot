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
        val exerciseCards = mutableListOf(
            ExerciseCard(0, Card(0, 1, "Птица была покрыта белыми перьями.", "The bird was covered with white feathers."), false),
            ExerciseCard(1, Card(1, 1, "Его ценность невелика.", "It is of little value."), false)
        )
        val exerciseData = ExerciseData(exerciseCards)
        return Observable.just(exerciseData)
    }

    override fun deleteAllExercises() {
        exerciseDao.deleteAll()
    }
}