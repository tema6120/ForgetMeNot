package com.odnovolov.forgetmenot.common.base

import android.app.Service
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class BaseService : Service() {
    private val serviceScope = MainScope()

    fun <T> Flow<T>.observe(onChange: (value: T) -> Unit) {
        serviceScope.launch {
            collect {
                if (isActive) {
                    onChange(it)
                }
            }
        }
    }

    fun <Order> ReceiveChannel<Order>.forEach(execute: (order: Order) -> Unit) {
        serviceScope.launch {
            for (order in this@forEach) {
                execute(order)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
    }
}