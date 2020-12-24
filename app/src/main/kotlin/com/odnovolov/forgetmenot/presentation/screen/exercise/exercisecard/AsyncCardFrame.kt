package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AsyncCardFrame @JvmOverloads constructor(
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
        setLayoutParams(LayoutParams(MATCH_PARENT, MATCH_PARENT))
        viewTreeObserver.addOnScrollChangedListener {
            if (isInflated && isCloseToScreen()) {
                executePendingActions()
            }
        }
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (isInflated && isCloseToScreen()) {
                executePendingActions()
            }
        }
    }

    var isInflated = false
        private set

    private var pendingActions = LinkedList<AsyncCardFrame.() -> Unit>()

    fun inflateAsync(layoutResId: Int) {
        GlobalScope.launch(Dispatchers.Default) {
            val view = LayoutInflater.from(context)
                .inflate(layoutResId, this@AsyncCardFrame, false)
            withContext(Dispatchers.Main.immediate) {
                isInflated = true
                if (isCloseToScreen()) {
                    addView(view)
                    executePendingActions()
                } else {
                    pendingActions.addFirst { addView(view) }
                }
            }
        }
    }

    fun invokeWhenReady(action: AsyncCardFrame.() -> Unit) {
        if (isInflated && isCloseToScreen()) {
            executePendingActions()
            action()
        } else {
            pendingActions.add(action)
        }
    }

    private fun isCloseToScreen(): Boolean {
        if (width == 0) return false
        val screen = 0..Resources.getSystem().displayMetrics.widthPixels
        val frame = x.toInt()..(x + width).toInt()
        return hasCommonPoints(screen, frame)
    }

    private fun hasCommonPoints(range1: IntRange, range2: IntRange): Boolean {
        return range1.contains(range2.first) || range1.contains(range2.last)
    }

    private fun executePendingActions() {
        if (pendingActions.isEmpty()) return
        pendingActions.forEach { action -> action() }
        pendingActions.clear()
    }
}