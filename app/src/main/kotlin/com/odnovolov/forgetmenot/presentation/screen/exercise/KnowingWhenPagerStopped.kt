package com.odnovolov.forgetmenot.presentation.screen.exercise

import androidx.viewpager2.widget.ViewPager2

class KnowingWhenPagerStopped {
    private val pendingActions: MutableList<() -> Unit> = ArrayList()
    private var isPagerStopped = true

    fun updateState(state: Int) {
        this.isPagerStopped = state == ViewPager2.SCROLL_STATE_IDLE
        if (isPagerStopped && pendingActions.isNotEmpty()) {
            pendingActions.forEach { action -> action() }
            pendingActions.clear()
        }
    }

    fun invokeWhenPagerStopped(action: () -> Unit) {
        if (isPagerStopped) {
            action()
        } else {
            pendingActions.add(action)
        }
    }
}