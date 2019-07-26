package com.odnovolov.forgetmenot.ui.exercisecreator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.odnovolov.forgetmenot.db.entity.ExerciseCardDbEntity
import com.odnovolov.forgetmenot.entity.ExerciseCard
import java.util.*

@Dao
abstract class ExerciseCreatorDao {

    @Query("DELETE FROM exercise_cards")
    abstract fun deleteAllExerciseCards()


    fun insertExerciseCards(exerciseCards: List<ExerciseCard>) {
        val exerciseCardDbEntities = exerciseCards.map { ExerciseCardDbEntity.fromExerciseCard(it) }
        insertInternal(exerciseCardDbEntities)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertInternal(exerciseCards: List<ExerciseCardDbEntity>)

    @Query("UPDATE decks SET lastOpenedAt = :lastOpenedAt WHERE deck_id = :deckId")
    abstract fun updateLastOpenedAt(lastOpenedAt: Calendar, deckId: Int)

}