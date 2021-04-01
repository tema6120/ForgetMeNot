package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.HintStatus.MaskingLettersAction.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*

open class ExerciseViewModel(
    private val exerciseState: Exercise.State,
    speakerImpl: SpeakerImpl,
    walkingModePreference: WalkingModePreference,
    private val exerciseSettings: ExerciseSettings,
    globalState: GlobalState
) {
    val isWalkingModeEnabled: Flow<Boolean> = globalState.flowOf(GlobalState::isWalkingModeEnabled)
        .flowOn(Dispatchers.Default)

    val exerciseCards: Flow<List<ExerciseCard>> =
        exerciseState.flowOf(Exercise.State::exerciseCards)

    val hasExerciseCards: Flow<Boolean> = exerciseCards.map { it.isNotEmpty() }

    val currentPosition: Int get() = exerciseState.currentPosition

    protected val currentExerciseCard: Flow<ExerciseCard> = combine(
        exerciseCards,
        exerciseState.flowOf(Exercise.State::currentPosition)
    ) { exerciseCards: List<ExerciseCard>, currentPosition: Int ->
        exerciseCards.getOrNull(currentPosition)
    }
        .filterNotNull()
        .distinctUntilChanged()
        .share()

    val gradeOfCurrentCard: Flow<Int> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::grade)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val isGradeEditedManually: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isGradeEditedManually)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val intervalItems: Flow<List<IntervalItem>?> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            val intervals = exerciseCard.base.deck.exercisePreference.intervalScheme?.intervals
            exerciseCard.base.card.flowOf(Card::grade)
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

    val isCurrentExerciseCardLearned: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::isLearned)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    private val hasQuestionSelection: Flow<Boolean> = exerciseState
        .flowOf(Exercise.State::questionSelection)
        .map { questionSelection: String -> questionSelection.isNotEmpty() }
        .distinctUntilChanged()

    private val hasAnswerSelection: Flow<Boolean> = exerciseState
        .flowOf(Exercise.State::answerSelection)
        .map { answerSelection: String -> answerSelection.isNotEmpty() }
        .distinctUntilChanged()

    private val isCurrentCardAnswered: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
                .map { isAnswerCorrect: Boolean? -> isAnswerCorrect != null }
        }
            .distinctUntilChanged()

    private val currentLanguages: Flow<Pair<Locale?, Locale?>> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            val pronunciationFlow: Flow<Pronunciation> =
                exerciseCard.base.deck.flowOf(Deck::exercisePreference)
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
                exerciseCard.base.flowOf(ExerciseCard.Base::isInverted)
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
        .flatMapLatest { language: Locale? -> speakerImpl.languageStatusOf(language) }
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

    val speakerEvents: Flow<SpeakerImpl.Event> = speakerImpl.events
        .flowOn(Dispatchers.Default)

    open val timerStatus: Flow<TimerStatus> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.deck.flowOf(Deck::exercisePreference)
                .flatMapLatest { exercisePreference: ExercisePreference ->
                    exercisePreference.flowOf(ExercisePreference::timeForAnswer)
                }
                .flatMapLatest { timeForAnswer: Int ->
                    if (timeForAnswer == NOT_TO_USE_TIMER) {
                        flowOf(TimerStatus.NotUsed)
                    } else {
                        combine(
                            exerciseCard.base.flowOf(ExerciseCard.Base::timeLeft),
                            exerciseCard.base.flowOf(ExerciseCard.Base::isExpired),
                            isWalkingModeEnabled
                        ) { timeLeft: Int,
                            isExpired: Boolean,
                            isWalkingModeEnabled: Boolean
                            ->
                            when {
                                isWalkingModeEnabled -> TimerStatus.OffBecauseWalkingMode
                                timeLeft > 0 -> TimerStatus.Ticking(timeLeft)
                                isExpired -> TimerStatus.TimeIsOver
                                else -> TimerStatus.Stopped
                            }
                        }
                    }
                }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val hintStatus: Flow<HintStatus> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            if (exerciseCard is QuizTestExerciseCard) {
                flowOf(HintStatus.Off)
            } else {
                combine(
                    exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
                    exerciseCard.base.card.flowOf(Card::isLearned),
                    isWalkingModeEnabled,
                    exerciseCard.base.flowOf(ExerciseCard.Base::hint),
                    exerciseState.flowOf(Exercise.State::hintSelection)
                ) { isAnswerCorrect: Boolean?,
                    isLearned: Boolean,
                    isWalkingModeEnabled: Boolean,
                    hint: String?,
                    hintSelection: HintSelection
                    ->
                    when {
                        isAnswerCorrect != null -> HintStatus.NotAccessibleBecauseCardIsAnswered
                        isLearned -> HintStatus.NotAccessibleBecauseCardIsLearned
                        else -> {
                            val isGettingVariantsAccessible = !isWalkingModeEnabled
                            val currentMaskingLettersAction = when {
                                hint == null -> MaskLetters
                                hintSelection.endIndex > hintSelection.startIndex -> UnmaskSelectedRegion
                                else -> UnmaskTheFirstLetter
                            }
                            HintStatus.Accessible(
                                isGettingVariantsAccessible,
                                currentMaskingLettersAction
                            )
                        }
                    }
                }
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val vibrateCommand: Flow<Unit> = currentExerciseCard.flatMapLatest { exerciseCard ->
        when (exerciseCard) {
            is OffTestExerciseCard, is ManualTestExerciseCard ->
                exerciseCard.base.flowOf(ExerciseCard.Base::isExpired)
                    .mapTwoLatest { wasExpired: Boolean, isExpiredNow: Boolean ->
                        if (!wasExpired && isExpiredNow) Unit else null
                    }
            else ->
                exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
                    .mapTwoLatest { wasCorrect: Boolean?, isCorrectNow: Boolean? ->
                        if (wasCorrect == null && isCorrectNow == false) Unit else null
                    }
        }
    }
        .filterNotNull()
        .flowOn(Dispatchers.Default)

    val learnedCardSoundNotification: Flow<Unit> =
        isWalkingModeEnabled.flatMapLatest { isWalkingModeEnabled: Boolean ->
            if (isWalkingModeEnabled) {
                currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
                    exerciseCard.base.card.flowOf(Card::isLearned)
                        .mapTwoLatest { wasLearned: Boolean, isLearnedNow: Boolean ->
                            if (!wasLearned && isLearnedNow) Unit else null
                        }
                }
            } else {
                flowOf(null)
            }
        }
            .filterNotNull()
            .flowOn(Dispatchers.Default)

    val keyGestureMap: Flow<Map<KeyGesture, KeyGestureAction>> =
        walkingModePreference.flowOf(WalkingModePreference::keyGestureMap)
            .flowOn(Dispatchers.Default)

    val cardPosition: Flow<String> = combine(
        exerciseState.flowOf(Exercise.State::currentPosition),
        exerciseCards
    ) { currentPosition: Int, exerciseCards: List<ExerciseCard> ->
        "${currentPosition + 1}/${exerciseCards.size}"
    }
        .debounce(10)
        .flowOn(Dispatchers.Default)

    val showProgressBar: Boolean
        get() = exerciseSettings.showProgressBar

    val showTextOfCardPosition: Boolean
        get() = exerciseSettings.showTextOfCardPosition

    val unansweredCardCount: Int
        get() = exerciseState.exerciseCards.count { exerciseCard: ExerciseCard ->
            !exerciseCard.isAnswered && !exerciseCard.base.card.isLearned
        }
}