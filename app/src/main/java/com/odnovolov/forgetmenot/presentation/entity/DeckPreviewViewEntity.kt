package com.odnovolov.forgetmenot.presentation.entity

import android.os.Parcelable
import com.odnovolov.forgetmenot.domain.entity.DeckPreview
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeckPreviewViewEntity(
    val deckId: Int,
    val deckName: String,
    val passedLaps: Int,
    val progressViewEntity: ProgressViewEntity
) : Parcelable {

    @Parcelize
    data class ProgressViewEntity(
        val learned: Int,
        val total: Int
    ): Parcelable {
        override fun toString(): String {
            return "$learned/$total"
        }

        fun toProgress() = DeckPreview.Progress(
            learned,
            total
        )

        companion object {
            fun fromProgress(progress: DeckPreview.Progress) = ProgressViewEntity(
                progress.learned,
                progress.total
            )
        }
    }

    fun toDeckPreview() = DeckPreview(
        deckId,
        deckName,
        passedLaps,
        progressViewEntity.toProgress()
    )

    companion object {
        fun fromDeckPreview(deckPreview: DeckPreview) = DeckPreviewViewEntity(
            deckPreview.deckId,
            deckPreview.deckName,
            deckPreview.passedLaps,
            ProgressViewEntity.fromProgress(deckPreview.progress)
        )
    }
}