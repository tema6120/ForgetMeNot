package com.odnovolov.forgetmenot.presentation.common

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState

interface Store {
    fun loadGlobalState(): GlobalState
    fun loadDeckReviewPreference(): DeckReviewPreference
    fun saveStateByRegistry()

    fun loadHomeScreenState(): HomeScreenState
    fun save(homeScreenState: HomeScreenState)
    fun deleteHomeScreenState()

    fun loadAddDeckState(): AddDeckInteractor.State
    fun save(addDeckInteractorState: AddDeckInteractor.State)
    fun deleteAddDeckState()

    fun loadAddDeckScreenState(): AddDeckScreenState
    fun save(addDeckScreenState: AddDeckScreenState)
    fun deleteAddDeckScreenState()

    fun loadExerciseState(globalState: GlobalState): Exercise.State
    fun save(exerciseState: Exercise.State)
    fun deleteExerciseState()
}