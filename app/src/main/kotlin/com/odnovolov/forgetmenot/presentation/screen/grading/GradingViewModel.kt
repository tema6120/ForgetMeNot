package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class GradingViewModel(
    deckSettingsState: DeckSettings.State
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

    val onFirstWrongAnswer: Flow<GradeChangeOnWrongAnswer> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::onFirstWrongAnswer)
        }

    val askAgain: Flow<Boolean> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::askAgain)
        }

    val onRepeatedCorrectAnswer: Flow<GradeChangeOnCorrectAnswer> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::onRepeatedCorrectAnswer)
        }

    val onRepeatedWrongAnswer: Flow<GradeChangeOnWrongAnswer> =
        grading.flatMapLatest { grading: Grading ->
            grading.flowOf(Grading::onRepeatedWrongAnswer)
        }
}