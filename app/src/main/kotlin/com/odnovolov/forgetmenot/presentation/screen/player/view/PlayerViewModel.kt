package com.odnovolov.forgetmenot.presentation.screen.player.view

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*

class PlayerViewModel(
    private val playerState: Player.State,
    speakerImpl: SpeakerImpl,
    globalState: GlobalState
) {
    val playingCards: Flow<List<PlayingCard>> =
        playerState.flowOf(Player.State::playingCards)
            .flowOn(Dispatchers.Default)

    val hasPlayingCards: Flow<Boolean> = playingCards.map { it.isNotEmpty() }

    private val currentPlayingCard: Flow<PlayingCard> = combine(
        playingCards,
        playerState.flowOf(Player.State::currentPosition)
    ) { playingCards: List<PlayingCard>, position: Int ->
        playingCards.getOrNull(position)
    }
        .filterNotNull()
        .distinctUntilChanged()
        .share()

    val cardPosition: Flow<String> = combine(
        playerState.flowOf(Player.State::currentPosition),
        playingCards
    ) { currentPosition: Int, playingCards: List<PlayingCard> ->
        "${currentPosition + 1}/${playingCards.size}"
    }
        .debounce(10)
        .flowOn(Dispatchers.Default)

    val gradeOfCurrentCard: Flow<Int> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            playingCard.card.flowOf(Card::grade)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

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
            .flowOn(Dispatchers.Default)

    val isCurrentCardLearned: Flow<Boolean> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            playingCard.card.flowOf(Card::isLearned)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

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

    private val currentLanguages: Flow<Pair<Locale?, Locale?>> =
        currentPlayingCard.flatMapLatest { playingCard: PlayingCard ->
            val pronunciationFlow: Flow<Pronunciation> =
                playingCard.deck.flowOf(Deck::exercisePreference)
                    .flatMapLatest { exercisePreference: ExercisePreference ->
                        exercisePreference.flowOf(ExercisePreference::pronunciation)
                    }
                    .share()
            val questionLanguageFlow: Flow<Locale?> =
                pronunciationFlow.flatMapLatest { pronunciation: Pronunciation ->
                    pronunciation.flowOf(Pronunciation::questionLanguage)
                }
            val answerLanguageFlow: Flow<Locale?> =
                pronunciationFlow.flatMapLatest { pronunciation: Pronunciation ->
                    pronunciation.flowOf(Pronunciation::answerLanguage)
                }
            combine(
                questionLanguageFlow,
                answerLanguageFlow,
                playingCard.flowOf(PlayingCard::isInverted)
            ) { questionLanguage: Locale?,
                answerLanguage: Locale?,
                isInverted: Boolean
                ->
                if (isInverted) answerLanguage to questionLanguage
                else questionLanguage to answerLanguage
            }
        }

    private val speakerLanguage: Flow<Locale?> =
        combine(
            currentLanguages,
            hasQuestionSelection,
            hasAnswerSelection,
            isCurrentCardAnswered
        ) { currentLanguages: Pair<Locale?, Locale?>,
            hasQuestionSelection: Boolean,
            hasAnswerSelection: Boolean,
            isAnswered: Boolean
            ->
            val (questionLanguage, answerLanguage) = currentLanguages
            when {
                hasQuestionSelection -> questionLanguage
                hasAnswerSelection -> answerLanguage
                isAnswered -> answerLanguage
                else -> questionLanguage
            }
        }
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
        .flowOn(Dispatchers.Default)

    val isSpeakerPreparingToPronounce: Flow<Boolean> =
        speakerImpl.state.flowOf(SpeakerImpl.State::isPreparingToSpeak)
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

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
        .flowOn(Dispatchers.Default)

    val speakerEvents: Flow<SpeakerImpl.Event> = speakerImpl.events
        .flowOn(Dispatchers.Default)

    val isPlaying: Flow<Boolean> = playerState.flowOf(Player.State::isPlaying)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    sealed class Laps {
        class SpecificNumberOfText(val text: String) : Laps()
        object Infinitely : Laps()
    }

    val laps: Flow<Laps> = combine(
        playerState.flowOf(Player.State::currentLap),
        globalState.flowOf(GlobalState::numberOfLapsInPlayer)
    ) { currentLap: Int, numberOfLapsInPlayer: Int ->
        val lapOrdinal = currentLap + 1
        when (numberOfLapsInPlayer) {
            Int.MAX_VALUE -> Laps.Infinitely
            1 -> Laps.SpecificNumberOfText("1")
            else -> Laps.SpecificNumberOfText("$lapOrdinal/$numberOfLapsInPlayer")
        }
    }

    val isCompleted: Flow<Boolean> = playerState.flowOf(Player.State::isCompleted)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    val currentPosition: Int get() = playerState.currentPosition
}