package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.*

class PronunciationViewModel(
    deckSettingsState: DeckSettings.State,
    speakerImpl: SpeakerImpl,
    screenState: PronunciationScreenState,
    pronunciationPreferences: PronunciationPreferences
) {
    val tip: Flow<Tip?> = screenState.flowOf(PronunciationScreenState::tip)

    private val currentPronunciation: Flow<Pronunciation> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciation)
        }
        .share()

    private data class FavorableLanguage(
        val language: Locale,
        val isFavorite: Boolean
    )

    private val availableLanguages: Flow<List<Locale>> = speakerImpl.state
        .flowOf(SpeakerImpl.State::availableLanguages)
        .map { availableLanguages: Set<Locale> ->
            val favoriteLanguages = pronunciationPreferences.favoriteLanguages
            availableLanguages.map { locale: Locale ->
                    val isFavorite = locale in favoriteLanguages
                    FavorableLanguage(locale, isFavorite)
                }
                .sortedWith(
                    compareByDescending<FavorableLanguage> { it.isFavorite }
                        .thenBy { it.language.displayName }
                )
                .map { it.language }
        }

    val selectedQuestionLanguage: Flow<Locale?> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::questionLanguage)
        }

    private var favoriteLanguages: Flow<Set<Locale>> =
        pronunciationPreferences.flowOf(PronunciationPreferences::favoriteLanguages)

    val displayedQuestionLanguages: Flow<List<DisplayedLanguage>> = combine(
        availableLanguages,
        selectedQuestionLanguage,
        favoriteLanguages
    ) { availableLanguages: List<Locale>,
        selectedQuestionLanguage: Locale?,
        favoriteLanguages: Set<Locale>
        ->
        val defaultLanguage = DisplayedLanguage(
            language = null,
            isSelected = selectedQuestionLanguage == null,
            isFavorite = null
        )
        val concreteLanguages = availableLanguages
            .map { language: Locale ->
                DisplayedLanguage(
                    language = language,
                    isSelected = selectedQuestionLanguage == language,
                    isFavorite = language in favoriteLanguages
                )
            }
        listOf(defaultLanguage) + concreteLanguages
    }

    val questionAutoSpeaking: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::questionAutoSpeaking)
        }

    val selectedAnswerLanguage: Flow<Locale?> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::answerLanguage)
        }

    val displayedAnswerLanguages: Flow<List<DisplayedLanguage>> = combine(
        availableLanguages,
        selectedAnswerLanguage,
        favoriteLanguages
    ) { availableLanguages: List<Locale>,
        selectedAnswerLanguage: Locale?,
        favoriteLanguages: Set<Locale> ->
        val defaultLanguage = DisplayedLanguage(
            language = null,
            isSelected = selectedAnswerLanguage == null,
            isFavorite = null
        )
        val concreteLanguages = availableLanguages
            .map { language: Locale ->
                DisplayedLanguage(
                    language = language,
                    isSelected = selectedAnswerLanguage == language,
                    isFavorite = language in favoriteLanguages
                )
            }
        listOf(defaultLanguage) + concreteLanguages
    }

    val answerAutoSpeaking: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::answerAutoSpeaking)
        }

    val speakTextInBrackets: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::speakTextInBrackets)
        }
}