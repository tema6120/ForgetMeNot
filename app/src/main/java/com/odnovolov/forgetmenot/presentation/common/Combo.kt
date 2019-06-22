package com.odnovolov.forgetmenot.presentation.common

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3

class Combo {

    companion object {
        fun <State1, State2> of(
            feature1: ObservableSource<State1>,
            feature2: ObservableSource<State2>
        ): ObservableSource<DoubleState<State1, State2>> {
            return Observable.combineLatest(
                feature1,
                feature2,
                BiFunction<State1, State2, DoubleState<State1, State2>>
                { state1, state2 ->
                    DoubleState(state1, state2)
                }
            )
        }

        fun <State1, State2, State3> of(
            feature1: ObservableSource<State1>,
            feature2: ObservableSource<State2>,
            feature3: ObservableSource<State3>
        ): ObservableSource<TripleState<State1, State2, State3>> {
            return Observable.combineLatest(
                feature1,
                feature2,
                feature3,
                Function3<State1, State2, State3, TripleState<State1, State2, State3>>
                { state1, state2, state3 ->
                    TripleState(state1, state2, state3)
                }
            )
        }
    }

    data class DoubleState<State1, State2>(
        val state1: State1,
        val state2: State2
    )

    data class TripleState<State1, State2, State3>(
        val state1: State1,
        val state2: State2,
        val state3: State3
    )
}

data class UiEventWitViewState<UiEvent, ViewState>(
    val uiEvent: UiEvent,
    val viewState: ViewState
)

fun <T1, T2> ObservableSource<T1>.withLatest(o1: ObservableSource<T2>): ObservableSource<Pair<T1, T2>> {
    return Observable.wrap(this)
        .withLatestFrom(o1, BiFunction { t1: T1, t2: T2 -> t1 to t2 })
}