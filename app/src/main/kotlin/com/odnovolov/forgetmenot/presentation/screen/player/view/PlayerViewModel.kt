package com.odnovolov.forgetmenot.presentation.screen.player.view

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus
import kotlinx.coroutines.flow.*
import java.util.*

class PlayerViewModel(
    private val playerState: Player.State,
    speakerImpl: SpeakerImpl,
    globalState: GlobalState
) {
    val playingCards: Flow<List<PlayingCard>> =
        playerState.flowOf(Player.State::playingCards)
            .flowOn(businessLogicThread)

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
            .distinctUntilChanged()
            .flowOn(businessLogicThread)

    val intervalItems: Flow<List<IntervalItem>?> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            val intervals = playingCard.deck.exercisePreference.intervalScheme?.intervals
            playingCard.card.flowOf(Card::grade)
                .map { currentGrade: Int ->
                    intervals?.map { interval: Interval ->
                        IntervalItem(
                            grade = interval.grade,
                            waitingPeriod = interval.value,
                            isSelected = currentGrade == interval.grade
                        )
                    }
                }
        }
            .distinctUntilChanged()
            .flowOn(businessLogicThread)

    val isCurrentCardLearned: Flow<Boolean> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            playingCard.card.flowOf(Card::isLearned)
        }
            .distinctUntilChanged()
            .flowOn(businessLogicThread)

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
            .distinctUntilChanged()

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
            val isReverse: Boolean = currentPlayingCard.isInverted
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
            .distinctUntilChanged()
            .share()

    private val languageStatus: Flow<LanguageStatus?> = speakerLanguage
        .flatMapLatest { language: Locale? ->
            speakerImpl.languageStatusOf(language)
        }
        .distinctUntilChanged()
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
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isSpeakerPreparingToPronounce: Flow<Boolean> =
        speakerImpl.state.flowOf(SpeakerImpl.State::isPreparingToSpeak)
            .distinctUntilChanged()
            .flowOn(businessLogicThread)

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
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val speakerEvents: Flow<SpeakerImpl.Event> = speakerImpl.events
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isPlaying: Flow<Boolean> = playerState.flowOf(Player.State::isPlaying)
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isInfinitePlaybackEnabled: Flow<Boolean> =
        globalState.flowOf(GlobalState::isInfinitePlaybackEnabled)
            .flowOn(businessLogicThread)

    val isCompleted: Flow<Boolean> = playerState.flowOf(Player.State::isCompleted)
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val currentPosition: Int get() = playerState.currentPosition
}