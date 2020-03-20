package com.odnovolov.forgetmenot.presentation.common

interface StateProvider<State> {
    fun load(): State
    fun save(state: State)
    fun delete()
}