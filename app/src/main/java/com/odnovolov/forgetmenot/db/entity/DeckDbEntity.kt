package com.odnovolov.forgetmenot.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.entity.Pronunciation
import java.util.*

@Entity(tableName = "decks")
data class DeckDbEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "deck_id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Calendar,

    @ColumnInfo(name = "last_opened_at")
    val lastOpenedAt: Calendar?,

    @Embedded
    val exercisePreferenceDbEntity: ExercisePreferenceDbEntity
) {
    fun toDeck(
        cards: List<Card>,
        pronunciation: Pronunciation?
    ) = Deck(
        id,
        name,
        cards,
        createdAt,
        lastOpenedAt,
        exercisePreferenceDbEntity.toExercisePreference(pronunciation)
    )

    companion object {
        fun fromDeck(deck: Deck) = DeckDbEntity(
            deck.id,
            deck.name,
            deck.createdAt,
            deck.lastOpenedAt,
            ExercisePreferenceDbEntity.fromExercisePreference(deck.exercisePreference)
        )
    }
}