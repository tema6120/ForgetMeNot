package com.odnovolov.forgetmenot.presentation.common.base

import android.app.Service
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

abstract class BaseService : Service() {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun <T> Flow<T>.observe(onEach: (value: T) -> Unit) {
        coroutineScope.launch {
            collect {
                if (isActive) {
                    onEach(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}