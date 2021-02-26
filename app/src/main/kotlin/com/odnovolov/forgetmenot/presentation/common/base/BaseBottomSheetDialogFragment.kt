package com.odnovolov.forgetmenot.presentation.common.base

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    var viewCoroutineScope: CoroutineScope? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPeekHeight()
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

    private fun setPeekHeight() {
        dialog?.setOnShowListener { dialog1 ->
            val bottomSheetDialog = dialog1 as BottomSheetDialog
            val bottomSheet: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels * 2 / 3
            }
        }
    }

    override fun onDestroyView() {
        viewCoroutineScope!!.cancel()
        viewCoroutineScope = null
        super.onDestroyView()
    }
}