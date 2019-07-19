package com.odnovolov.forgetmenot.presentation.common

import androidx.lifecycle.LiveData

interface ViewModel<State, Action, Event> {
    val state: State
    fun action(): LiveData<Action>? = null
    fun onEvent(event: Event) {}
}