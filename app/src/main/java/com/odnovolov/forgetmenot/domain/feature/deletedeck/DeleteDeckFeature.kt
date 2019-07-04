package com.odnovolov.forgetmenot.domain.feature.deletedeck

import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.State
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Wish
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Wish.*
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Effect
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Effect.DeckDeleted
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DeleteDeckFeature(
    deckRepository: DeckRepository,
    mainThreadScheduler: Scheduler
) : ActorReducerFeature<Wish, Effect, State, Nothing>(
    initialState = State,
    actor = ActorImpl(deckRepository, mainThreadScheduler),
    reducer = ReducerImpl()
) {
    sealed class Wish {
        data class DeleteDeck(val deckId: Int) : Wish()
    }

    class ActorImpl(
        private val deckRepository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Wish, Effect> {

        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            is DeleteDeck -> deleteDeck(wish.deckId)
        }

        private fun deleteDeck(deckId: Int): Observable<Effect> {
            return Observable.fromCallable { deckRepository.delete(deckId) }
                .map { DeckDeleted as Effect }
                .schedule()
        }

        private fun <T> Observable<T>.schedule(): Observable<T> {
            return this.subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Effect {
        object DeckDeleted : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = state
    }

    object State
}
