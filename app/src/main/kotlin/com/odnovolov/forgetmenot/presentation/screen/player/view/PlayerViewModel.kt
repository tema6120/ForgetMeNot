package com.odnovolov.forgetmenot.presentation.screen.player.view

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*

class PlayerViewModel(
    private val playerState: Player.State,
    speakerImpl: SpeakerImpl
) {
    val playingCards: Flow<List<PlayingCard>> =
        playerState.flowOf(Player.State::playingCards)

    private val currentPlayingCard: Flow<PlayingCard> = combine(
        playingCards,
        playerState.flowOf(Player.State::currentPosition)
    ) { playingCards: List<PlayingCard>, position: Int ->
        playingCards[position]
    }
        .distinctUntilChanged()
        .share()

    val gradeOfCurrentCard: Flow<Int> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            playingCard.card.flowOf(Card::grade)
        }

    val isCurrentPlayingCardLearned: Flow<Boolean> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            playingCard.card.flowOf(Card::isLearned)
        }

    private val hasQuestionSelection: Flow<Boolean> = playerState
        .flowOf(Player.State::questionSelection)
        .map { questionSelection: String -> questionSelection.isNotEmpty() }
        .distinctUntilChanged()

    private val hasAnswerSelection: Flow<Boolean> = playerState
        .flowOf(Player.State::answerSelection)
        .map { answerSelection: String -> answerSelection.isNotEmpty() }
        .distinctUntilChanged()

    private val isCurrentCardAnswered: Flow<Boolean> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            playingCard.flowOf(PlayingCard::isAnswerDisplayed)
        }

    private val speakerLanguage: Flow<Locale?> =
        combine(
            currentPlayingCard,
            hasQuestionSelection,
            hasAnswerSelection,
            isCurrentCardAnswered
        ) { currentPlayingCard: PlayingCard,
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
            val isReverse: Boolean = currentPlayingCard.isReverse
            val pronunciation: Pronunciation =
                currentPlayingCard.deck.exercisePreference.pronunciation
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

    val isPlaying: Flow<Boolean> = playerState.flowOf(Player.State::isPlaying)

    val currentPosition: Int get() = playerState.currentPosition
}