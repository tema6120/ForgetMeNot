package com.odnovolov.forgetmenot.screen.home

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.home.HomeControllerQueries
import com.odnovolov.forgetmenot.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.screen.home.HomeOrder.*

class HomeController : BaseController<HomeEvent, HomeOrder>() {
    private val queries: HomeControllerQueries = database.homeControllerQueries

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is SearchTextChanged -> {
                queries.setSearchText(event.searchText)
            }

            DisplayOnlyWithTasksCheckboxClicked -> {
                queries.toogleDisplayOnlyWithTasks()
            }

            is DeckButtonClicked -> {
                with(database.exerciseCardsInitQueries) {
                    createTableExerciseCard()
                    cleanTableExerciseCard()
                    initExerciseCard(event.deckId)
                }
                with(database.exerciseInitQueries) {
                    createTableExercise()
                    cleanTableExercise()
                    initExercise(event.deckId)
                    createViewCurrentExerciseCard()
                    createViewExercisePronunciation()
                    createTriggerObserveAnswerAutoSpeakEvent()
                }
                // TODO move 'setLastOpenedAt()' to Exercise screen
                queries.updateLastOpenedAt(event.deckId)
                issueOrder(NavigateToExercise)
            }

            is SetupDeckMenuItemClicked -> {
                with(database.deckSettingsInitQueries) {
                    createTableDeckSettingsState()
                    cleanTableDeckSettingsState()
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