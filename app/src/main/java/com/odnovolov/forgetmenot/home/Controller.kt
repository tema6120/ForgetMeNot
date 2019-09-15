package com.odnovolov.forgetmenot.home

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.home.HomeEvent.*
import com.odnovolov.forgetmenot.home.HomeOrder.*
import java.util.*

class HomeController : BaseController<HomeEvent, HomeOrder>() {
    private val queries: HomeControllerQueries = database.homeControllerQueries

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is SearchTextChanged -> {
                queries.setSearchText(event.searchText)
            }

            is DeckButtonClicked -> {
                with(database.exerciseInitQueries) {
                    dropTableExerciseState()
                    createTableExercise()
                    initExercise(event.deckId)
                    createViewCurrentExerciseCard()
                    createViewExercisePronunciation()
                }
                with(database.exerciseCardsInitQueries) {
                    dropTableExerciseCard()
                    createTableExerciseCard()
                    initExerciseCard(event.deckId)
                }
                // TODO move 'setLastOpenedAt()' to Exercise screen
                queries.updateLastOpenedAt(event.deckId)
                issueOrder(NavigateToExercise)
            }

            is SetupDeckMenuItemClicked -> {
                with(database.deckSettingsInitQueries) {
                    dropTableDeckSettingsState()
                    createTableDeckSettingsState()
                    initDeckSettingsState(event.deckId)
                    createViewCurrentExercisePreference()
                    createViewCurrentPronunciation()
                    createTriggerPreventRemovalOfDefaultExercisePreference()
                    createTriggerOnTryToModifyDefaultExercisePreference()
                    createTriggerSetDefaultExercisePreferenceIfNeed()
                    createTriggerOnDeleteExercisePreference()
                    createTriggerDeleteUnusedIndividualExercisePreference()
                    createTriggerClenupAfterDeleteOfExercisePreference()
                }
                issueOrder(NavigateToDeckSettings)
            }

            is DeleteDeckMenuItemClicked -> {
                val deckId = event.deckId

                queries.dropTableCardBackup()
                queries.createTableCardBackup()
                queries.addCardBackup(deckId)

                queries.dropTableDeckBackup()
                queries.createTableDeckBackup()
                queries.addDeckBackup(deckId)

                queries.deleteDeck(deckId)
                issueOrder(ShowDeckWasDeletedMessage)
            }

            DeckIsDeletedSnackbarCancelActionClicked -> {
                queries.restoreDeck()
                queries.restoreCard()
            }
        }
    }
}