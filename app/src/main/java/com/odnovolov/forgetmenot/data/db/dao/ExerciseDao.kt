package com.odnovolov.forgetmenot.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.odnovolov.forgetmenot.data.db.entity.ExerciseCardDbRow
import com.odnovolov.forgetmenot.data.db.toExerciseDbRow
import com.odnovolov.forgetmenot.domain.feature.exercise.Exercise

@Dao
abstract class ExerciseDao {
    fun insert(exercise: Exercise) {
        val exerciseCardDbRows: List<ExerciseCardDbRow> = exercise.exerciseCards
                .map { exerciseCard -> exerciseCard.toExerciseDbRow() }
        insertInternal(exerciseCardDbRows)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertInternal(exerciseCards: List<ExerciseCardDbRow>)

    @Query("DELETE FROM exercise_cards")
    abstract fun deleteAll()
}