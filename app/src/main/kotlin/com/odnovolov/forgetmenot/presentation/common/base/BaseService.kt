package com.odnovolov.forgetmenot.presentation.common.base

import android.app.Service
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class BaseService : Service() {
    private val serviceScope = MainScope()

    fun <T> Flow<T>.observe(onEach: (value: T) -> Unit) {
        serviceScope.launch {
            collect {
                if (isActive) {
                    onEach(it)
                }
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
    }
}