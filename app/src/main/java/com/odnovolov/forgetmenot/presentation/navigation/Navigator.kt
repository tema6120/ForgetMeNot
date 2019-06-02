package com.odnovolov.forgetmenot.presentation.navigation

import io.reactivex.functions.Consumer

interface Navigator : Consumer<Navigator.Event> {
    enum class Event {
        NAVIGATE_TO_EXERCISE
    }
}