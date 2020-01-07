package com.odnovolov.forgetmenot.screen.home

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.QuizComposer
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
                if (queries.hasAnySelectedDeckId().executeAsOne()) {
                    toggleDeckSelection(event.deckId)
                } else {
                    startExercise(event.deckId)
                }
            }

            is DeckButtonLongClicked -> {
                toggleDeckSelection(event.deckId)
            }

            is SetupDeckMenuItemClicked -> {
                queries.clearDeckSelection()

                queries.cleanDeckSettingsState()
                queries.initDeckSettingsState(event.deckId)
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

            StartExerciseMenuItemClicked -> {
                startExercise(deckId = -1)
            }

            is SelectAllDecksMenuItemClicked -> {
                event.displayedCardIds.forEach(queries::addDeckToDeckSelection)
            }

            ActionModeFinished -> {
                queries.clearDeckSelection()
            }
        }
    }

    private fun startExercise(deckId: Long) {
        queries.cleanExerciseCard()
        queries.initExerciseCard(deckId)
        if (!queries.isThereAnyExerciseCard().executeAsOne()) {
            issueOrder(ShowNoCardsReadyForExercise)
            return
        }
        queries.cleanQuiz()
        QuizComposer.composeWhereItNeeds()
        queries.cleanAnswerInput()
        queries.initAnswerInput()
        queries.cleanExercise()
        queries.initExercise()
        queries.clearDeckSelection()
        queries.updateLastOpenedAt(deckId)
        issueOrder(NavigateToExercise)
    }

    private fun toggleDeckSelection(deckId: Long) {
        if (queries.hasDeckInDeckSelection(deckId).executeAsOne()) {
            queries.deleteDeckFromDeckSelection(deckId)
        } else {
            queries.addDeckToDeckSelection(deckId)
        }
    }
}