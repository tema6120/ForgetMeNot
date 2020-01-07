package com.odnovolov.forgetmenot.common.base

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    val fragmentScope = MainScope()
    var viewScope: CoroutineScope? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewScope = MainScope()
    }

    fun <T> Flow<T>.observe(coroutineScope: CoroutineScope = viewScope!!,
                            onChange: (value: T) -> Unit) {
        coroutineScope.launch {
            collect {
                onChange(it)
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
        super.onDestroyView()
        viewScope!!.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentScope.cancel()
    }

}