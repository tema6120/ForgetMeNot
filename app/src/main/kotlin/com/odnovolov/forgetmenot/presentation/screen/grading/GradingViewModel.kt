package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingScreenState.DialogPurpose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class GradingViewModel(
    deckSettingsState: DeckSettings.State,
    screenState: GradingScreenState
) {
    private val grading: Flow<Grading> =
        deckSettingsState.deck.flowOf(Deck::exercisePreference)
            .flatMapLatest { exercisePreference: ExercisePreference ->
                exercisePreference.flowOf(ExercisePreference::grading)
            }
            .share()

    val onFirstCorrectAnswer: Flow<GradeChangeOnCorrectAnswer> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::onFirstCorrectAnswer)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val onFirstWrongAnswer: Flow<GradeChangeOnWrongAnswer> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::onFirstWrongAnswer)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val askAgain: Flow<Boolean> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::askAgain)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val onRepeatedCorrectAnswer: Flow<GradeChangeOnCorrectAnswer> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::onRepeatedCorrectAnswer)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val onRepeatedWrongAnswer: Flow<GradeChangeOnWrongAnswer> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::onRepeatedWrongAnswer)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val dialogPurpose: Flow<GradingScreenState.DialogPurpose?> =
        screenState.flowOf(GradingScreenState::dialogPurpose)
            .flowOn(Dispatchers.Default)

    fun getGradeChangeItems(
        gradeChangeOnCorrectAnswerToDisplayText: (GradeChangeOnCorrectAnswer) -> String,
        gradeChangeOnWrongAnswerToDisplayText: (GradeChangeOnWrongAnswer) -> String
    ): Flow<List<GradeChangeItem>> =
        grading.flatMapLatest { currentGrading: Grading ->
            combine(
                currentGrading.asFlow(),
                dialogPurpose
            ) { grading: Grading, dialogPurpose: GradingScreenState.DialogPurpose? ->
                if (dialogPurpose == null) return@combine emptyList()
                val selectedGradeChange: GradeChange = when (dialogPurpose) {
                    ToChangeGradingOnFirstCorrectAnswer -> grading.onFirstCorrectAnswer
                    ToChangeGradingOnFirstWrongAnswer -> grading.onFirstWrongAnswer
                    ToChangeGradingOnRepeatedCorrectAnswer -> grading.onRepeatedCorrectAnswer
                    ToChangeGradingOnRepeatedWrongAnswer -> grading.onRepeatedWrongAnswer
                }
                when (dialogPurpose) {
                    ToChangeGradingOnFirstCorrectAnswer, ToChangeGradingOnRepeatedCorrectAnswer -> {
                        GradeChangeOnCorrectAnswer.values()
                            .map { gradeChange: GradeChangeOnCorrectAnswer ->
                                GradeChangeItem(
                                    gradeChange,
                                    text = gradeChangeOnCorrectAnswerToDisplayText(gradeChange),
                                    isSelected = gradeChange == selectedGradeChange
                                )
                            }
                    }
                    ToChangeGradingOnFirstWrongAnswer, ToChangeGradingOnRepeatedWrongAnswer -> {
                        GradeChangeOnWrongAnswer.values()
                            .map { gradeChange: GradeChangeOnWrongAnswer ->
                                GradeChangeItem(
                                    gradeChange,
                                    text = gradeChangeOnWrongAnswerToDisplayText(gradeChange),
                                    isSelected = gradeChange == selectedGradeChange
                                )
                            }
                    }
                }
            }
        }
            .flowOn(Dispatchers.Default)
}