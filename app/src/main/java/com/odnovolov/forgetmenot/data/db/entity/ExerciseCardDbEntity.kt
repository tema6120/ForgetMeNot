package com.odnovolov.forgetmenot.data.db.entity

import androidx.room.*
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.ExerciseCard

@Entity(
    tableName = "exercise_cards",
    foreignKeys = [
        ForeignKey(
            entity = CardDbEntity::class,
            parentColumns = ["card_id"],
            childColumns = ["card_id_fk"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("card_id_fk")]
)
data class ExerciseCardDbEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "exercise_card_id")
    val id: Int,

    @ColumnInfo(name = "card_id_fk")
    val cardId: Int,

    @ColumnInfo(name = "is_answered")
    val isAnswered: Boolean = false
) {
    fun toExerciseCard(card: Card) = ExerciseCard(id, card, isAnswered)

    companion object {
        fun fromExerciseCard(exerciseCard: ExerciseCard) =
            ExerciseCardDbEntity(exerciseCard.id, exerciseCard.card.id, exerciseCard.isAnswered)
    }
}