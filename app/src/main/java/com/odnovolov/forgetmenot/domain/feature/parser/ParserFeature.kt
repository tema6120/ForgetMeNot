package com.odnovolov.forgetmenot.domain.feature.parser

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.parser.ParserFeature.*
import com.odnovolov.forgetmenot.domain.feature.parser.ParserFeature.Effect.*
import com.odnovolov.forgetmenot.domain.feature.parser.ParserFeature.Wish.Parse
import java.io.InputStream
import io.reactivex.Observable

class ParserFeature : ActorReducerFeature<Wish, Effect, State, Nothing>(
        initialState = State(),
        actor = ActorImpl(),
        reducer = ReducerImpl()
) {
    sealed class Wish {
        data class Parse(val inputStream: InputStream) : Wish()
    }

    data class State(
            val isWorking: Boolean = false,
            val deck: Deck? = null
    )

    sealed class Effect {
        object StartedParsing : Effect()
        data class ParsedDeck(val deck: Deck) : Effect()
        data class ErrorParsing(val throwable: Throwable) : Effect()
    }

    class ActorImpl : Actor<State, Wish, Effect> {
        override fun invoke(state: State, wish: Wish): Observable<out Effect> {
            return when (wish) {
                is Parse -> Observable.fromCallable { Parser.parse(wish.inputStream) }
                        .map { deck: Deck -> ParsedDeck(deck) as Effect }
                        .startWith(Observable.just(StartedParsing as Effect))
                        .onErrorReturn { throwable: Throwable -> ErrorParsing(throwable) }
            }
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                StartedParsing -> state.copy(
                        isWorking = true
                )
                is ParsedDeck -> state.copy(
                        isWorking = false,
                        deck = effect.deck
                )
                is ErrorParsing -> state.copy(
                        isWorking = false
                )
            }
        }
    }
}