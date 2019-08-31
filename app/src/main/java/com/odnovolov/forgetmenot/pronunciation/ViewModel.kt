package com.odnovolov.forgetmenot.pronunciation

import com.odnovolov.forgetmenot.common.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.*

class PronunciationViewModel {
    private val queries: PronunciationViewModelQueries = database.pronunciationViewModelQueries

    private val pronunciation: Flow<Pronunciation> = queries
        .getPronunciation()
        .asFlow()
        .mapToOne()

    private val availableLanguages: Flow<List<Locale>> = queries
        .getAvailableLanguages(mapper = { databaseValue: String? ->
            if (databaseValue == null) {
                emptyList()
            } else {
                listOfLocalesAdapter.decode(databaseValue)
            }
        })
        .asFlow()
        .mapToOne()

    val selectedQuestionLanguage: Flow<Locale?> = pronunciation.map { it.questionLanguage }

    val dropdownQuestionLanguages: Flow<List<DropdownLanguage>> =
        availableLanguages.combine(selectedQuestionLanguage)
        { availableLanguages: List<Locale>, selectedQuestionLanguage: Locale? ->
            val defaultLanguage = DropdownLanguage(
                language = null,
                isSelected = selectedQuestionLanguage == null
            )
            val concreteLanguages = availableLanguages
                .map { language: Locale ->
                    DropdownLanguage(
                        language = language,
                        isSelected = selectedQuestionLanguage == language
                    )
                }
            listOf(defaultLanguage) + concreteLanguages
        }

    val questionAutoSpeak: Flow<Boolean> = pronunciation.map { it.questionAutoSpeak }

    val selectedAnswerLanguage: Flow<Locale?> = pronunciation.map { it.answerLanguage }

    val dropdownAnswerLanguages: Flow<List<DropdownLanguage>> =
        availableLanguages.combine(selectedAnswerLanguage)
        { availableLanguages: List<Locale>, selectedAnswerLanguage: Locale? ->
            val defaultLanguage = DropdownLanguage(
                language = null,
                isSelected = selectedAnswerLanguage == null
            )
            val concreteLanguages = availableLanguages
                .map { language: Locale ->
                    DropdownLanguage(
                        language = language,
                        isSelected = selectedAnswerLanguage == language
                    )
                }
            listOf(defaultLanguage) + concreteLanguages
        }

    val answerAutoSpeak: Flow<Boolean> = pronunciation.map { it.answerAutoSpeak }
}