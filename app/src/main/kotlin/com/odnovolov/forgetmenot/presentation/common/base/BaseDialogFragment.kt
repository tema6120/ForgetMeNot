package com.odnovolov.forgetmenot.presentation.common.base

import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseDialogFragment : DialogFragment() {
    var viewScope: CoroutineScope? = null

    fun onCreateDialog() {
        viewScope = MainScope()
    }

    fun <T> Flow<T>.observe(onEach: (value: T) -> Unit) {
        viewScope!!.launch {
            collect {
                if (isActive) {
                    onEach(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        viewScope!!.cancel()
        super.onDestroyView()
    }
}