package com.odnovolov.forgetmenot.common.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

class LinearLayoutWithInterceptTouchEventListener @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    var onTouch: (() -> Unit)? = null

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        onTouch?.invoke()
        return super.onInterceptTouchEvent(ev)
    }
}