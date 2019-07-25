package com.odnovolov.forgetmenot.common

import androidx.lifecycle.LiveData

interface ViewModel<State, Action, Event> {
    val state: State
    val action: LiveData<Action>? get() = null
    fun onEvent(event: Event) {}
}