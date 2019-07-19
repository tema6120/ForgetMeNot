package com.odnovolov.forgetmenot.presentation.common

import androidx.lifecycle.LiveData

class ActionSender<Action> {

    private val singleLiveEvent = SingleLiveEvent<Action>()

    fun send(action: Action) {
        singleLiveEvent.value = action
    }

    fun asLiveData(): LiveData<Action> = singleLiveEvent

}