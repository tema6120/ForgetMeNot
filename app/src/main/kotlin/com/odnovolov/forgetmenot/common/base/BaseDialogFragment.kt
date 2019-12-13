package com.odnovolov.forgetmenot.common.base

import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

open class BaseDialogFragment : DialogFragment() {

    private val fragmentScope = MainScope()

    fun <T> Flow<T>.observe(onChange: (value: T) -> Unit) {
        fragmentScope.launch {
            collect {
                if (isActive) {
                    onChange(it)
                }
            }
        }
    }

    override fun onDestroy() {
        fragmentScope.cancel()
        super.onDestroy()
    }

}