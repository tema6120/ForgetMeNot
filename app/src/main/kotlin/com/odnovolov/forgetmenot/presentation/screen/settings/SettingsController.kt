package com.odnovolov.forgetmenot.presentation.screen.settings

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState
import com.odnovolov.forgetmenot.presentation.screen.settings.SettingsEvent.*

class SettingsController(
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val fullscreenPreference: FullscreenPreference,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<SettingsEvent, Nothing>() {
    override fun handle(event: SettingsEvent) {
        when (event) {
            FullscreenInExerciseCheckboxClicked -> {
                with(fullscreenPreference) {
                    isEnabledInExercise = !isEnabledInExercise
                }
            }

            FullscreenInRepetitionCheckboxClicked -> {
                with(fullscreenPreference) {
                    isEnabledInCardPlayer = !isEnabledInCardPlayer
                }
            }

            FullscreenInOtherPlacesCheckboxClicked -> {
                with(fullscreenPreference) {
                    isEnabledInOtherPlaces = !isEnabledInOtherPlaces
                }
            }

            CardAppearanceButtonClicked -> {
                navigator.navigateToCardAppearanceSettings {
                    val tenRandomCards = ArrayList<Card>()
                    run loop@{
                        repeat(10) {
                            val randomDeck: Deck = globalState.decks.randomOrNull() ?: return@loop
                            val randomCard: Card = randomDeck.cards.randomOrNull() ?: return@loop
                            tenRandomCards.add(randomCard)
                        }
                    }
                    if (tenRandomCards.isEmpty()) {
                        val defaultExampleCard = Card(generateId(), "Question", "Answer")
                        tenRandomCards.add(defaultExampleCard)
                    }
                    val screenState = CardAppearanceScreenState(tenRandomCards)
                    CardAppearanceDiScope.create(screenState)
                }
            }

            ExerciseButtonClicked -> {
                navigator.navigateToExerciseSettings()
            }

            WalkingModeButtonClicked -> {
                navigator.navigateToWalkingModeSettingsFromNavHost()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}