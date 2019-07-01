package com.odnovolov.forgetmenot.presentation.entity

import android.os.Parcelable
import com.odnovolov.forgetmenot.domain.entity.ExerciseCard
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExerciseCardViewEntity(
    val id: Int = 0,
    val cardViewEntity: CardViewEntity,
    var isAnswered: Boolean = false
) : Parcelable {

    fun toExerciseCard() = ExerciseCard(
        id,
        cardViewEntity.toCard(),
        isAnswered
    )

    companion object {
        fun fromExerciseCard(exerciseCard: ExerciseCard) = ExerciseCardViewEntity(
            exerciseCard.id,
            CardViewEntity.fromCard(exerciseCard.card),
            exerciseCard.isAnswered
        )
    }
}