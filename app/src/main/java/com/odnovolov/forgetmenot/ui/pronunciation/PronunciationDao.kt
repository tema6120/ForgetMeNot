package com.odnovolov.forgetmenot.ui.pronunciation

import androidx.room.Dao
import androidx.room.Insert
import com.odnovolov.forgetmenot.db.entity.PronunciationDbEntity
import com.odnovolov.forgetmenot.entity.Pronunciation

@Dao
abstract class PronunciationDao {
    fun savePronunciation(pronunciation: Pronunciation): Long {
        val pronunciationDbEntity = PronunciationDbEntity.fromPronunciation(pronunciation)
        return savePronunciationInternal(pronunciationDbEntity)
    }

    @Insert
    abstract fun savePronunciationInternal(pronunciationDbEntity: PronunciationDbEntity): Long
}