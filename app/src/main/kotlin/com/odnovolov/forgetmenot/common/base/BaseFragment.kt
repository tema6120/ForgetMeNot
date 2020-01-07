package com.odnovolov.forgetmenot.common.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseFragment : Fragment() {

    val fragmentScope = MainScope()
    var viewScope: CoroutineScope? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewScope = MainScope()
    }

    fun <T> Flow<T>.observe(
        coroutineScope: CoroutineScope = viewScope!!,
        onChange: (value: T) -> Unit
    ) {
        coroutineScope.launch {
            collect {
                if (isActive) {
                    onChange(it)
                }
            }
        }
    }

    fun <T> Flow<T>.observe(
        coroutineScope: CoroutineScope = viewScope!!,
        onChange: (value: T) -> Unit,
        afterFirst: (value: T) -> Unit
    ) {
        coroutineScope.launch {
            var isFirst = true
            collect {
                if (isActive) {
                    onChange(it)
                    if (isFirst) {
                        afterFirst(it)
                        isFirst = false
                    }
                }
            }
        }
    }

    fun <Order> ReceiveChannel<Order>.forEach(execute: (order: Order) -> Unit) {
        viewScope?.launch {
            for (order in this@forEach) {
                execute(order)
            }
        }
    }

    override fun onDestroyView() {
        viewScope!!.cancel()
        super.onDestroyView()
    }

    override fun onDestroy() {
        fragmentScope.cancel()
        super.onDestroy()
    }

}