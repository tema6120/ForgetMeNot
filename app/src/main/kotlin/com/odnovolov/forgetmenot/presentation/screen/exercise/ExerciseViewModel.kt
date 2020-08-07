package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*

class ExerciseViewModel(
    private val exerciseState: Exercise.State,
    speakerImpl: SpeakerImpl,
    walkingModePreference: WalkingModePreference,
    globalState: GlobalState
) {
    val isWalkingModeEnabled: Flow<Boolean> = globalState.flowOf(GlobalState::isWalkingModeEnabled)

    val exerciseCards: Flow<List<ExerciseCard>> =
        exerciseState.flowOf(Exercise.State::exerciseCards)

    val currentPosition: Int get() = exerciseState.currentPosition

    private val currentExerciseCard: Flow<ExerciseCard> = combine(
        exerciseCards,
        exerciseState.flowOf(Exercise.State::currentPosition)
    ) { exerciseCards: List<ExerciseCard>, currentPosition: Int ->
        exerciseCards[currentPosition]
    }
        .distinctUntilChanged()
        .share()

    val isCurrentExerciseCardLearned: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::isLearned)
        }

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

    private val speakerLanguage: Flow<Locale?> =
        combine(
            currentExerciseCard,
            hasQuestionSelection,
            hasAnswerSelection,
            isCurrentCardAnswered
        ) { currentExerciseCard: ExerciseCard,
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
            val isReverse: Boolean = currentExerciseCard.base.isReverse
            val pronunciation: Pronunciation =
                currentExerciseCard.base.deck.exercisePreference.pronunciation
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

    val hintStatus: Flow<HintStatus> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            val isQuizTestExerciseCard: Boolean = exerciseCard is QuizTestExerciseCard
            combine(
                exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
                exerciseCard.base.card.flowOf(Card::isLearned)
            ) { isAnswerCorrect: Boolean?, isLearned: Boolean ->
                when {
                    isQuizTestExerciseCard || isLearned -> HintStatus.Off
                    isAnswerCorrect == null -> HintStatus.Accessible
                    else -> HintStatus.NotAccessible
                }
            }
        }

    enum class HintStatus {
        Accessible,
        NotAccessible,
        Off
    }

    val timeLeft: Flow<Int?> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            val isTimerEnabled = exerciseCard.base.deck.exercisePreference.timeForAnswer > 0
            combine(
                exerciseCard.base.flowOf(ExerciseCard.Base::timeLeft),
                exerciseCard.base.card.flowOf(Card::isLearned),
                isWalkingModeEnabled
            ) { timeLeft: Int,
                isLearned: Boolean,
                isWalkingModeEnabled: Boolean
                ->
                when {
                    isWalkingModeEnabled || isLearned || !isTimerEnabled -> null
                    else -> timeLeft
                }
            }
        }

    val levelOfKnowledgeForCurrentCard: Flow<Int> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::levelOfKnowledge)
        }

    val isLevelOfKnowledgeEditedManually: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isLevelOfKnowledgeEditedManually)
        }

    val keyGestureMap: Flow<Map<KeyGesture, KeyGestureAction>> =
        walkingModePreference.flowOf(WalkingModePreference::keyGestureMap)
}