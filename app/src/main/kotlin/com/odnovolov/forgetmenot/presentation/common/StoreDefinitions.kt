package com.odnovolov.forgetmenot.presentation.common

// Lifetime of user session corresponds to lifetime of MainActivity's savedInstanceState
interface UserSessionTermStateProvider<State> {
    fun load(): State
    fun save(state: State)
}

interface LongTermStateProvider<State> {
    fun load(): State
}

interface LongTermStateSaver {
    fun saveStateByRegistry()
}