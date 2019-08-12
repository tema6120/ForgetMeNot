package com.odnovolov.forgetmenot.ui.pronunciation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import com.odnovolov.forgetmenot.db.entity.PronunciationDbEntity
import com.odnovolov.forgetmenot.entity.Pronunciation

@Dao
abstract class PronunciationDao {
    fun insertPronunciation(pronunciation: Pronunciation): Long {
        val pronunciationDbEntity = PronunciationDbEntity.fromPronunciation(pronunciation)
        return savePronunciationInternal(pronunciationDbEntity)
    }

    @Insert
    abstract fun savePronunciationInternal(pronunciationDbEntity: PronunciationDbEntity): Long

    fun updatePronunciation(pronunciation: Pronunciation) {
        val pronunciationDbEntity = PronunciationDbEntity.fromPronunciation(pronunciation)
        updatePronunciationInternal(pronunciationDbEntity)
    }

    @Update
    abstract fun updatePronunciationInternal(pronunciationDbEntity: PronunciationDbEntity)

}