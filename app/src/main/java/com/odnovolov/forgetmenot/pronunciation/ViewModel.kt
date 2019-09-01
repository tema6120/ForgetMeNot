package com.odnovolov.forgetmenot.pronunciation

import com.odnovolov.forgetmenot.common.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.*

class PronunciationViewModel {
    private val queries: PronunciationViewModelQueries = database.pronunciationViewModelQueries

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

    val currentPronunciation: Flow<Pronunciation> = queries
        .getCurrentPronunciation()
        .asFlow()
        .mapToOne()

    val isSavePronunciationButtonEnabled: Flow<Boolean> = currentPronunciation
        .map { it.id != 0L && it.name.isEmpty() }

    val sharedPronunciations: Flow<List<Pronunciation>> = queries
        .getSharedPronunciations()
        .asFlow()
        .mapToList()

    val selectedQuestionLanguage: Flow<Locale?> = currentPronunciation.map { it.questionLanguage }

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

    val questionAutoSpeak: Flow<Boolean> = currentPronunciation.map { it.questionAutoSpeak }

    val selectedAnswerLanguage: Flow<Locale?> = currentPronunciation.map { it.answerLanguage }

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

    val answerAutoSpeak: Flow<Boolean> = currentPronunciation.map { it.answerAutoSpeak }
}