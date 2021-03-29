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
                    DbKeys.ANSWER_TEXT_SIZE,
                    DbKeys.CARD_TEXT_OPACITY_IN_LIGHT_THEME,
                    DbKeys.CARD_TEXT_OPACITY_IN_DARK_THEME
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
        val textOpacityInLightTheme = keyValues[DbKeys.CARD_TEXT_OPACITY_IN_LIGHT_THEME]
            ?.toFloat()
            ?: CardAppearance.DEFAULT_TEXT_OPACITY_IN_LIGHT_THEME
        val textOpacityInDarkTheme = keyValues[DbKeys.CARD_TEXT_OPACITY_IN_DARK_THEME]
            ?.toFloat()
            ?: CardAppearance.DEFAULT_TEXT_OPACITY_IN_DARK_THEME
        return CardAppearance(
            questionTextAlignment,
            questionTextSize,
            answerTextAlignment,
            answerTextSize,
            textOpacityInLightTheme,
            textOpacityInDarkTheme
        )
    }
}