package com.odnovolov.forgetmenot.presentation.common.base

import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseDialogFragment : DialogFragment() {
    var viewCoroutineScope: CoroutineScope? = null

    fun onCreateDialog() {
        viewCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    fun <T> Flow<T>.observe(onEach: (value: T) -> Unit) {
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
        super.onDestroyView()
    }
}