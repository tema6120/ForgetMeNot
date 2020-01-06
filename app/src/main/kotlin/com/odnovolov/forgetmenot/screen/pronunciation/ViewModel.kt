package com.odnovolov.forgetmenot.screen.pronunciation

import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.Preset
import com.odnovolov.forgetmenot.common.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.*

class PronunciationViewModel {
    private val queries: PronunciationViewModelQueries = database.pronunciationViewModelQueries

    private val availableLanguages: Flow<List<Locale>> = queries
        .getAvailableLanguages(mapper = { databaseValue: String? ->
            listOfLocalesAdapter.decode(databaseValue)
        })
        .asFlow()
        .mapToOne()

    val currentPronunciation: Flow<Pronunciation> = queries
        .getCurrentPronunciation()
        .asFlow()
        .mapToOne()

    val isSavePronunciationButtonEnabled: Flow<Boolean> = currentPronunciation
        .map { it.id != 0L && it.name.isEmpty() }

    val availablePronunciations: Flow<List<Preset>> = queries
        .getAvailablePronunciations(::Preset)
        .asFlow()
        .mapToList()

    val isNamePresetDialogVisible: Flow<Boolean> = queries
        .isNamePresetDialogVisible()
        .asFlow()
        .mapToOne()

    val dialogInputCheckResult: Flow<NameCheckResult> = queries
        .getDialogInputCheckResult()
        .asFlow()
        .mapToOne()
        .map { databaseValue: String -> nameCheckResultAdapter.decode(databaseValue) }

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

    val doNotSpeakTextInBrackets: Flow<Boolean> = currentPronunciation.map {
        it.doNotSpeakTextInBrackets
    }
}