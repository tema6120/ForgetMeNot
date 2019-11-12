package com.odnovolov.forgetmenot.common.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class TextViewWithObservableSelection @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : TextView(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private var selectedText: String = ""
        set(value) {
            if (field != value) {
                field = value
                observer?.invoke(value)
            }
        }

    private var observer: ((String) -> Unit)? = null
        set(value) {
            field = value
            field?.invoke(selectedText)
        }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        val startIndex = minOf(selStart, selEnd)
        val endIndex = maxOf(selStart, selEnd)
        selectedText = text.toString().substring(startIndex, endIndex)
    }

    fun observeSelectedText(observer: ((String) -> Unit)?) {
        this.observer = observer
    }
}