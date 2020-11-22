package com.odnovolov.forgetmenot.presentation.common.base

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    var viewCoroutineScope: CoroutineScope? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    inline fun <T> Flow<T>.observe(crossinline onEach: (value: T) -> Unit = {}) {
        viewCoroutineScope!!.launch {
            collect {
                if (isActive) {
                    onEach(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        viewCoroutineScope!!.cancel()
        viewCoroutineScope = null
        super.onDestroyView()
    }
}