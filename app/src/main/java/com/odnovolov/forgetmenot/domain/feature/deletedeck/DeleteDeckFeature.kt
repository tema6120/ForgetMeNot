package com.odnovolov.forgetmenot.domain.feature.deletedeck

import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Wish.*
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.*
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Effect.*
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.News.*
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DeleteDeckFeature(
    deckRepository: DeckRepository,
    mainThreadScheduler: Scheduler
) : ActorReducerFeature<Wish, Effect, State, News>(
    initialState = State,
    actor = ActorImpl(deckRepository, mainThreadScheduler),
    reducer = ReducerImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    sealed class Wish {
        data class DeleteDeck(val deckId: Int) : Wish()
        object RestoreDeck : Wish()
    }

    class ActorImpl(
        private val deckRepository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Wish, Effect> {

        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            is DeleteDeck -> deleteDeck(wish.deckId)
            RestoreDeck -> restoreDeck()
        }

        private fun deleteDeck(deckId: Int): Observable<Effect> {
            return Observable
                .fromCallable { deckRepository.createBackupAndDeleteDeckInTransaction(deckId) }
                .map { numberOfDeletedDecks ->
                    if (numberOfDeletedDecks == 1) DeletingIsCompleted
                    else DeletingIsAborted
                }
                .schedule()
        }

        private fun restoreDeck(): Observable<Effect> {
            return Observable.fromCallable { deckRepository.restoreLastDeletedDeckFromBackup() }
                .map { RestoringIsCompleted as Effect }
                .schedule()
        }

        private fun <T> Observable<T>.schedule(): Observable<T> {
            return this
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Effect {
        object DeletingIsCompleted : Effect()
        object DeletingIsAborted : Effect()
        object RestoringIsCompleted : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = state
    }

    object State

    class NewsPublisherImpl : NewsPublisher<Wish, Effect, State, News?> {
        override fun invoke(wish: Wish, effect: Effect, state: State): News? = when (effect) {
            DeletingIsCompleted -> DeckDeleted
            DeletingIsAborted -> DeckIsNotDeleted
            RestoringIsCompleted -> DeckRestored
        }
    }

    sealed class News {
        object DeckDeleted : News()
        object DeckIsNotDeleted : News()
        object DeckRestored : News()
    }
}
