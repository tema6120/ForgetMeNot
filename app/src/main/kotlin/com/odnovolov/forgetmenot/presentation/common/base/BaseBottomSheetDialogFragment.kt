package com.odnovolov.forgetmenot.presentation.common.base

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    var viewScope: CoroutineScope? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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