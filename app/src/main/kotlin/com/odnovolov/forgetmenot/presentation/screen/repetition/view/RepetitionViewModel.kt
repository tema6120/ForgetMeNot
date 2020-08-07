package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*

class RepetitionViewModel(
    private val repetitionState: Repetition.State,
    speakerImpl: SpeakerImpl
) {
    val repetitionCards: Flow<List<RepetitionCard>> =
        repetitionState.flowOf(Repetition.State::repetitionCards)

    private val currentRepetitionCard: Flow<RepetitionCard> = combine(
        repetitionCards,
        repetitionState.flowOf(Repetition.State::repetitionCardPosition)
    ) { repetitionCards: List<RepetitionCard>, position: Int ->
        repetitionCards[position]
    }
        .distinctUntilChanged()
        .share()

    val levelOfKnowledgeForCurrentCard: Flow<Int> =
        currentRepetitionCard.flatMapLatest { repetitionCard: RepetitionCard ->
            repetitionCard.card.flowOf(Card::levelOfKnowledge)
        }

    val isCurrentRepetitionCardLearned: Flow<Boolean> =
        currentRepetitionCard.flatMapLatest { repetitionCard: RepetitionCard ->
            repetitionCard.card.flowOf(Card::isLearned)
        }

    private val hasQuestionSelection: Flow<Boolean> = repetitionState
        .flowOf(Repetition.State::questionSelection)
        .map { questionSelection: String -> questionSelection.isNotEmpty() }
        .distinctUntilChanged()

    private val hasAnswerSelection: Flow<Boolean> = repetitionState
        .flowOf(Repetition.State::answerSelection)
        .map { answerSelection: String -> answerSelection.isNotEmpty() }
        .distinctUntilChanged()

    private val isCurrentCardAnswered: Flow<Boolean> =
        currentRepetitionCard.flatMapLatest { repetitionCard: RepetitionCard ->
            repetitionCard.flowOf(RepetitionCard::isAnswered)
        }

    private val speakerLanguage: Flow<Locale?> =
        combine(
            currentRepetitionCard,
            hasQuestionSelection,
            hasAnswerSelection,
            isCurrentCardAnswered
        ) { currentRepetitionCard: RepetitionCard,
            hasQuestionSelection: Boolean,
            hasAnswerSelection: Boolean,
            isAnswered: Boolean
            ->
            val needToSpeakQuestion = when {
                hasQuestionSelection -> true
                hasAnswerSelection -> false
                isAnswered -> false
                else -> true
            }
            val isReverse: Boolean = currentRepetitionCard.isReverse
            val pronunciation: Pronunciation =
                currentRepetitionCard.deck.exercisePreference.pronunciation
            val language: Locale? = if (needToSpeakQuestion && !isReverse
                || !needToSpeakQuestion && isReverse
            ) {
                pronunciation.questionLanguage
            } else {
                pronunciation.answerLanguage
            }
            language
        }
            .flowOn(Dispatchers.Default)
            .share()

    private val languageStatus: Flow<LanguageStatus?> = speakerLanguage
        .flatMapLatest { language: Locale? ->
            speakerImpl.languageStatusOf(language)
        }
        .share()

    val speakingStatus: Flow<SpeakingStatus> = combine(
        speakerImpl.state.flowOf(SpeakerImpl.State::status),
        languageStatus,
        speakerImpl.state.flowOf(SpeakerImpl.State::isSpeaking)
    ) { status: Status, languageStatus: LanguageStatus?, isSpeaking: Boolean ->
        when {
            status == Status.Initialization -> SpeakingStatus.NotSpeaking
            isSpeaking -> SpeakingStatus.Speaking
            status == Status.FailedToInitialize
                    || languageStatus == LanguageStatus.NotSupported
                    || languageStatus == LanguageStatus.MissingData -> SpeakingStatus.CannotSpeak
            else -> SpeakingStatus.NotSpeaking
        }
    }

    val isSpeakerPreparingToPronounce: Flow<Boolean> =
        speakerImpl.state.flowOf(SpeakerImpl.State::isPreparingToSpeak)

    val reasonForInabilityToSpeak: Flow<ReasonForInabilityToSpeak?> = combine(
        speakerImpl.state.flowOf(SpeakerImpl.State::status),
        speakerLanguage,
        languageStatus,
        speakerImpl.state.flowOf(SpeakerImpl.State::ttsEngine),
        speakerImpl.state.flowOf(SpeakerImpl.State::defaultLanguage)
    ) { status: Status,
        speakerLanguage: Locale?,
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
                    speakerLanguage ?: defaultLanguage
                )
            }
            languageStatus == LanguageStatus.MissingData -> {
                MissingDataForLanguage(
                    speakerLanguage ?: defaultLanguage
                )
            }
            else -> null
        }
    }

    val speakerEvents: Flow<SpeakerImpl.Event> = speakerImpl.events

    val isPlaying: Flow<Boolean> = repetitionState.flowOf(Repetition.State::isPlaying)

    val repetitionCardPosition: Int get() = repetitionState.repetitionCardPosition
}