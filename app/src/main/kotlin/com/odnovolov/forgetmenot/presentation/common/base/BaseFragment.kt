package com.odnovolov.forgetmenot.presentation.common.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.presentation.common.DialogTimeCapsule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseFragment : Fragment() {
    var viewCoroutineScope: CoroutineScope? = null
    val dialogTimeCapsule = DialogTimeCapsule()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    inline fun <T> Flow<T>.observe(crossinline onEach: (value: T) -> Unit) {
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        dialogTimeCapsule.restore(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dialogTimeCapsule.save(outState)
    }
}