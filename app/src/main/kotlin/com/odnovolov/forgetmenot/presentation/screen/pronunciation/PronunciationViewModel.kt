package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState.WhatIsPronounced.ANSWER
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState.WhatIsPronounced.QUESTION
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.*

class PronunciationViewModel(
    deckSettingsState: DeckSettings.State,
    private val pronunciationScreenState: PronunciationScreenState,
    speakerImpl: SpeakerImpl
) {
    val isQuestionPronounced: Flow<Boolean> = speakerImpl.state
        .flowOf(SpeakerImpl.State::isSpeaking)
        .map { isSpeaking: Boolean ->
            isSpeaking && pronunciationScreenState.whatIsPronounced == QUESTION
        }

    val isQuestionPreparingToBePronounced: Flow<Boolean> = speakerImpl.state
        .flowOf(SpeakerImpl.State::isPreparingToPronounce)
        .map { isPreparing: Boolean ->
            isPreparing && pronunciationScreenState.whatIsPronounced == QUESTION
        }

    val isAnswerPronounced: Flow<Boolean> = speakerImpl.state
        .flowOf(SpeakerImpl.State::isSpeaking)
        .map { isSpeaking: Boolean ->
            isSpeaking && pronunciationScreenState.whatIsPronounced == ANSWER
        }

    val isAnswerPreparingToBePronounced: Flow<Boolean> = speakerImpl.state
        .flowOf(SpeakerImpl.State::isPreparingToPronounce)
        .map { isPreparing: Boolean ->
            isPreparing && pronunciationScreenState.whatIsPronounced == ANSWER
        }

    private val availableLanguages: Flow<Set<Locale>> = speakerImpl.state
        .flowOf(SpeakerImpl.State::availableLanguages)

    private val currentPronunciation: Flow<Pronunciation> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciation)
        }
        .share()

    val selectedQuestionLanguage: Flow<Locale?> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::questionLanguage)
        }

    val displayedQuestionLanguages: Flow<List<DisplayedLanguage>> = combine(
        availableLanguages,
        selectedQuestionLanguage
    ) { availableLanguages: Set<Locale>, selectedQuestionLanguage: Locale? ->
        val defaultLanguage = DisplayedLanguage(
            language = null,
            isSelected = selectedQuestionLanguage == null
        )
        val concreteLanguages = availableLanguages
            .map { language: Locale ->
                DisplayedLanguage(
                    language = language,
                    isSelected = selectedQuestionLanguage == language
                )
            }
        listOf(defaultLanguage) + concreteLanguages
    }

    val questionAutoSpeak: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::questionAutoSpeak)
        }

    val selectedAnswerLanguage: Flow<Locale?> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::answerLanguage)
        }

    val displayedAnswerLanguages: Flow<List<DisplayedLanguage>> = combine(
        availableLanguages,
        selectedAnswerLanguage
    ) { availableLanguages: Set<Locale>, selectedAnswerLanguage: Locale? ->
        val defaultLanguage = DisplayedLanguage(
            language = null,
            isSelected = selectedAnswerLanguage == null
        )
        val concreteLanguages = availableLanguages
            .map { language: Locale ->
                DisplayedLanguage(
                    language = language,
                    isSelected = selectedAnswerLanguage == language
                )
            }
        listOf(defaultLanguage) + concreteLanguages
    }

    val answerAutoSpeak: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::answerAutoSpeak)
        }

    val speakTextInBrackets: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::speakTextInBrackets)
        }
}