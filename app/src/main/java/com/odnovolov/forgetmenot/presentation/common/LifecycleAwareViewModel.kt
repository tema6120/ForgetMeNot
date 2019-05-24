package com.odnovolov.forgetmenot.presentation.common

import androidx.lifecycle.ViewModel
import com.badoo.mvicore.binder.lifecycle.ManualLifecycle

open class LifecycleAwareViewModel : ViewModel() {

    val lifecycle = ManualLifecycle()

    init {
        lifecycle.begin()
    }

    override fun onCleared() {
        super.onCleared()
        lifecycle.end()
    }
}