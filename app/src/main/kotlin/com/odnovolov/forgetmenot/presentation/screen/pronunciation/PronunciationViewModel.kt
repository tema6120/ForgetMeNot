package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState.WhatIsPronounced.ANSWER
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState.WhatIsPronounced.QUESTION
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.*

class PronunciationViewModel(
    deckSettingsState: DeckSettings.State,
    private val screenState: PronunciationScreenState,
    speakerImpl: SpeakerImpl
) {
    private val currentPronunciation: Flow<Pronunciation> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciation)
        }
        .share()

    private val availableLanguages: Flow<Set<Locale>> = speakerImpl.state
        .flowOf(SpeakerImpl.State::availableLanguages)

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

    private val questionLanguageStatus: Flow<LanguageStatus?> = selectedQuestionLanguage
        .flatMapLatest { language: Locale? -> speakerImpl.languageStatusOf(language) }
        .share()

    val isQuestionPreparingToBePronounced: Flow<Boolean> = speakerImpl.state
        .flowOf(SpeakerImpl.State::isPreparingToSpeak)
        .map { isPreparing: Boolean -> isPreparing && screenState.whatIsPronounced == QUESTION }

    val questionSpeakingStatus: Flow<SpeakingStatus> = combine(
        speakerImpl.state.flowOf(SpeakerImpl.State::status),
        questionLanguageStatus,
        speakerImpl.state.flowOf(SpeakerImpl.State::isSpeaking)
    ) { status: Status, languageStatus: LanguageStatus?, isSpeaking: Boolean ->
        when {
            status == Status.Initialization -> SpeakingStatus.NotSpeaking
            isSpeaking && screenState.whatIsPronounced == QUESTION -> SpeakingStatus.Speaking
            status == Status.FailedToInitialize
                    || languageStatus == LanguageStatus.NotSupported
                    || languageStatus == LanguageStatus.MissingData -> SpeakingStatus.CannotSpeak
            else -> SpeakingStatus.NotSpeaking
        }
    }

    val reasonForInabilityToSpeakQuestion: Flow<ReasonForInabilityToSpeak?> = combine(
        speakerImpl.state.flowOf(SpeakerImpl.State::status),
        selectedQuestionLanguage,
        questionLanguageStatus,
        speakerImpl.state.flowOf(SpeakerImpl.State::ttsEngine),
        speakerImpl.state.flowOf(SpeakerImpl.State::defaultLanguage)
    ) { status: Status,
        language: Locale?,
        languageStatus: LanguageStatus?,
        ttsEngine: String?,
        defaultLanguage: Locale
        ->
        when {
            status == Status.FailedToInitialize -> {
                FailedToInitializeSpeaker(ttsEngine)
            }
            languageStatus == LanguageStatus.NotSupported -> {
                LanguageIsNotSupported(
                    ttsEngine,
                    language ?: defaultLanguage
                )
            }
            languageStatus == LanguageStatus.MissingData -> {
                MissingDataForLanguage(
                    language ?: defaultLanguage
                )
            }
            else -> null
        }
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

    private val answerLanguageStatus: Flow<LanguageStatus?> = selectedAnswerLanguage
        .flatMapLatest { language: Locale? -> speakerImpl.languageStatusOf(language) }
        .share()

    val isAnswerPreparingToBePronounced: Flow<Boolean> = speakerImpl.state
        .flowOf(SpeakerImpl.State::isPreparingToSpeak)
        .map { isPreparing: Boolean -> isPreparing && screenState.whatIsPronounced == ANSWER }

    val answerSpeakingStatus: Flow<SpeakingStatus> = combine(
        speakerImpl.state.flowOf(SpeakerImpl.State::status),
        answerLanguageStatus,
        speakerImpl.state.flowOf(SpeakerImpl.State::isSpeaking)
    ) { status: Status, languageStatus: LanguageStatus?, isSpeaking: Boolean ->
        when {
            status == Status.Initialization -> SpeakingStatus.NotSpeaking
            isSpeaking && screenState.whatIsPronounced == ANSWER -> SpeakingStatus.Speaking
            status == Status.FailedToInitialize
                    || languageStatus == LanguageStatus.NotSupported
                    || languageStatus == LanguageStatus.MissingData -> SpeakingStatus.CannotSpeak
            else -> SpeakingStatus.NotSpeaking
        }
    }

    val reasonForInabilityToSpeakAnswer: Flow<ReasonForInabilityToSpeak?> = combine(
        speakerImpl.state.flowOf(SpeakerImpl.State::status),
        selectedAnswerLanguage,
        answerLanguageStatus,
        speakerImpl.state.flowOf(SpeakerImpl.State::ttsEngine),
        speakerImpl.state.flowOf(SpeakerImpl.State::defaultLanguage)
    ) { status: Status,
        language: Locale?,
        languageStatus: LanguageStatus?,
        ttsEngine: String?,
        defaultLanguage: Locale
        ->
        when {
            status == Status.FailedToInitialize -> {
                FailedToInitializeSpeaker(ttsEngine)
            }
            languageStatus == LanguageStatus.NotSupported -> {
                LanguageIsNotSupported(
                    ttsEngine,
                    language ?: defaultLanguage
                )
            }
            languageStatus == LanguageStatus.MissingData -> {
                MissingDataForLanguage(
                    language ?: defaultLanguage
                )
            }
            else -> null
        }
    }

    val speakTextInBrackets: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::speakTextInBrackets)
        }

    val speakerEvents: Flow<SpeakerImpl.Event> = speakerImpl.events
}