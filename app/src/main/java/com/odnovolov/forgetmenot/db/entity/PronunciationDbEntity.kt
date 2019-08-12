package com.odnovolov.forgetmenot.db.entity

import androidx.room.*
import com.odnovolov.forgetmenot.entity.Pronunciation
import java.util.*

@Entity(tableName = "pronunciations")
data class PronunciationDbEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pronunciation_id")
    val id: Int = 0,

    @ColumnInfo(name = "pronunciation_name")
    val name: String,

    @ColumnInfo(name = "question_language")
    val questionLanguage: Locale? = null,

    @ColumnInfo(name = "question_auto_speak")
    val questionAutoSpeak: Boolean = false,

    @ColumnInfo(name = "answer_language")
    val answerLanguage: Locale? = null,

    @ColumnInfo(name = "answer_auto_speak")
    val answerAutoSpeak: Boolean = false
) {
    fun toPronunciation() = Pronunciation(
        id,
        name,
        questionLanguage,
        questionAutoSpeak,
        answerLanguage,
        answerAutoSpeak
    )

    companion object {
        fun fromPronunciation(pronunciation: Pronunciation) = PronunciationDbEntity(
            pronunciation.id,
            pronunciation.name,
            pronunciation.questionLanguage,
            pronunciation.questionAutoSpeak,
            pronunciation.answerLanguage,
            pronunciation.answerAutoSpeak
        )
    }
}