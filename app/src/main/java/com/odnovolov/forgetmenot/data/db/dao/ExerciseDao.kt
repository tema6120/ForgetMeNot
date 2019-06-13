package com.odnovolov.forgetmenot.data.db.dao

import androidx.room.*
import com.odnovolov.forgetmenot.data.db.entity.ExerciseCardDbEntity
import com.odnovolov.forgetmenot.data.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseData
import io.reactivex.Observable

@Dao
abstract class ExerciseDao {

    // Create

    fun insert(exerciseData: ExerciseData) {
        exerciseData.exerciseCards
            .map { exerciseCard -> ExerciseCardDbEntity.fromExerciseCard(exerciseCard) }
            .let { exerciseCardDbEntity -> insertInternal(exerciseCardDbEntity) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertInternal(exerciseCards: List<ExerciseCardDbEntity>)

    // Read

    fun observeExercise(): Observable<ExerciseData> {
        return observeExerciseCardsInternal()
            .map { queryResponse: List<ExerciseCardQueryData> ->
                queryResponse.map { queryData: ExerciseCardQueryData ->
                    queryData.toExerciseCard()
                }
            }
            .map { exerciseCardList: List<ExerciseCard> ->
                ExerciseData(exerciseCardList as MutableList)
            }
    }

    @Query("SELECT * FROM exercise_cards LEFT JOIN cards ON card_id_fk = card_id")
    abstract fun observeExerciseCardsInternal(): Observable<List<ExerciseCardQueryData>>

    class ExerciseCardQueryData {
        @Embedded
        lateinit var cardDbEntity: CardDbEntity

        @Embedded
        lateinit var exerciseCardDbEntity: ExerciseCardDbEntity

        fun toExerciseCard(): ExerciseCard {
            return exerciseCardDbEntity.toExerciseCard(cardDbEntity.toCard())
        }
    }
    
    // Update
    
    @Transaction
    open fun updateExerciseCard(exerciseCard: ExerciseCard) {
        updateCardDbEntityBesidesDeckIdInternal(
            exerciseCard.card.id,
            exerciseCard.card.ordinal,
            exerciseCard.card.question,
            exerciseCard.card.answer
        )
        val exerciseCardDbEntity = ExerciseCardDbEntity.fromExerciseCard(exerciseCard)
        updateExerciseCardDbEntityInternal(exerciseCardDbEntity)
    }

    @Query("UPDATE cards SET ordinal = :ordinal, question = :question, answer = :answer WHERE card_id = :cardId")
    abstract fun updateCardDbEntityBesidesDeckIdInternal(
        cardId: Int,
        ordinal: Int,
        question: String,
        answer: String
    )

    @Update
    abstract fun updateExerciseCardDbEntityInternal(exerciseCardDbEntity: ExerciseCardDbEntity)

    // Delete

    @Query("DELETE FROM exercise_cards")
    abstract fun deleteAll()

}