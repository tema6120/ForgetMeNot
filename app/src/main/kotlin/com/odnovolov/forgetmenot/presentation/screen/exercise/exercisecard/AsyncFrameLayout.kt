package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AsyncFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    init {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    private var isInflated = false
    private var pendingActions: MutableList<AsyncFrameLayout.() -> Unit> = ArrayList()

    fun inflateAsync(layoutResId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val view = LayoutInflater.from(context)
                .inflate(layoutResId, this@AsyncFrameLayout, false)
            withContext(Dispatchers.Main.immediate) {
                addView(view)
                isInflated = true
                pendingActions.forEach { action -> action() }
                pendingActions.clear()
            }
        }
    }

    fun invokeWhenInflated(action: AsyncFrameLayout.() -> Unit) {
        if (isInflated) {
            action()
        } else {
            pendingActions.add(action)
        }
    }
}