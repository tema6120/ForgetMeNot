package com.odnovolov.forgetmenot.db.entity

import androidx.room.ColumnInfo
import com.odnovolov.forgetmenot.entity.ExercisePreference
import com.odnovolov.forgetmenot.entity.Pronunciation

data class ExercisePreferenceDbEntity(
    @ColumnInfo(name = "random_order")
    val randomOrder: Boolean = true,

    @ColumnInfo(name = "pronunciation_id_key")
    val pronunciationId: Int? = null
) {
    fun toExercisePreference(pronunciation: Pronunciation?) = ExercisePreference(
        randomOrder,
        pronunciation
    )

    companion object {
        fun fromExercisePreference(exercisePreference: ExercisePreference) = ExercisePreferenceDbEntity(
            exercisePreference.randomOrder
        )
    }
}