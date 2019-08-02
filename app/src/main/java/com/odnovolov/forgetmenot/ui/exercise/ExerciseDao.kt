package com.odnovolov.forgetmenot.ui.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Transaction
import com.odnovolov.forgetmenot.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.db.entity.ExerciseCardDbEntity
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.ExerciseCard

@Dao
abstract class ExerciseDao {

    fun getExerciseCards(): LiveData<List<ExerciseCard>> {
        return Transformations.map(getExerciseCardsInternal()) { roughExerciseCards: List<RoughExerciseCard> ->
            roughExerciseCards.map { roughExerciseCard: RoughExerciseCard ->
                val card: Card = roughExerciseCard.cardDbEntity.toCard()
                roughExerciseCard.exerciseCardDbEntity.toExerciseCard(card)
            }
        }
    }

    @Transaction
    @Query("SELECT * FROM exercise_cards LEFT JOIN cards ON card_id_fk = card_id")
    abstract fun getExerciseCardsInternal(): LiveData<List<RoughExerciseCard>>

    class RoughExerciseCard {
        @Embedded
        lateinit var cardDbEntity: CardDbEntity

        @Embedded
        lateinit var exerciseCardDbEntity: ExerciseCardDbEntity
    }

    @Query("UPDATE exercise_cards SET is_answered = 1 WHERE exercise_card_id = :exerciseCardId")
    abstract fun setAnswered(exerciseCardId: Int)

    @Query("UPDATE cards SET lap = :lap WHERE card_id = :cardId")
    abstract fun setLap(lap: Int, cardId: Int)

    @Query("UPDATE cards SET is_learned = :isCardLearned WHERE card_id = :cardId")
    abstract fun setIsCardLearned(isCardLearned: Boolean, cardId: Int)

}