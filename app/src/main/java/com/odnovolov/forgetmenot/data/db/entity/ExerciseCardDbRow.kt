package com.odnovolov.forgetmenot.data.db.entity

import androidx.room.*

@Entity(
        tableName = "exercise_cards",
        foreignKeys = [
            ForeignKey(
                    entity = CardDbRow::class,
                    parentColumns = ["card_id"],
                    childColumns = ["card_id"]
            )
        ],
        indices = [Index("card_id")]
)
data class ExerciseCardDbRow(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "exercise_card_id")
        val exerciseCardId: Int,

        @ColumnInfo(name = "card_id")
        val cardId: Int,

        @ColumnInfo(name = "is_answered")
        val isAnswered: Boolean = false
)