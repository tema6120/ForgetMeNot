package com.odnovolov.forgetmenot.presentation.common

// 'Short term' corresponds to lifetime of MainActivity's savedInstanceState
interface ShortTermStateProvider<State> {
    fun load(): State
    fun save(state: State)
}

interface LongTermStateProvider<State> {
    fun load(): State
}

interface LongTermStateSaver {
    fun saveStateByRegistry()
}