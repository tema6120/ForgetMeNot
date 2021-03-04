package com.odnovolov.forgetmenot.presentation.common.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseFragment : Fragment() {
    protected var viewCoroutineScope: CoroutineScope? = null
    private var hasSavedInstanceState = false
    private var numberOfOnViewCreatedInvocation = 0
    protected val isViewFirstCreated: Boolean
        get() = !hasSavedInstanceState && numberOfOnViewCreatedInvocation <= 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hasSavedInstanceState = savedInstanceState != null
        numberOfOnViewCreatedInvocation++
        viewCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        super.onViewCreated(view, savedInstanceState)
    }

    protected inline fun <T> Flow<T>.observe(crossinline onEach: (value: T) -> Unit = {}) {
        viewCoroutineScope?.launch {
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