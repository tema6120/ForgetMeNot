package com.odnovolov.forgetmenot.persistence.longterm.cardappearance

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.toEnumOrNull
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardTextAlignment

class CardAppearanceProvider(
    private val database: Database
) : LongTermStateProvider<CardAppearance> {
    override fun load(): CardAppearance {
        val keyValues: Map<Long, String?> = database.keyValueQueries
            .selectValues(
                keys = listOf(
                    DbKeys.QUESTION_TEXT_ALIGNMENT,
                    DbKeys.QUESTION_TEXT_SIZE,
                    DbKeys.ANSWER_TEXT_ALIGNMENT,
                    DbKeys.ANSWER_TEXT_SIZE
                )
            )
            .executeAsList()
            .associate { (key, value) -> key to value }
        val questionTextAlignment: CardTextAlignment = keyValues[DbKeys.QUESTION_TEXT_ALIGNMENT]
            ?.toEnumOrNull<CardTextAlignment>()
            ?: CardAppearance.DEFAULT_QUESTION_TEXT_ALIGNMENT
        val questionTextSize: Int = keyValues[DbKeys.QUESTION_TEXT_SIZE]
            ?.toInt()
            ?: CardAppearance.DEFAULT_QUESTION_TEXT_SIZE
        val answerTextAlignment: CardTextAlignment = keyValues[DbKeys.ANSWER_TEXT_ALIGNMENT]
            ?.toEnumOrNull<CardTextAlignment>()
            ?: CardAppearance.DEFAULT_ANSWER_TEXT_ALIGNMENT
        val answerTextSize: Int = keyValues[DbKeys.ANSWER_TEXT_SIZE]
            ?.toInt()
            ?: CardAppearance.DEFAULT_ANSWER_TEXT_SIZE
        return CardAppearance(
            questionTextAlignment,
            questionTextSize,
            answerTextAlignment,
            answerTextSize
        )
    }
}