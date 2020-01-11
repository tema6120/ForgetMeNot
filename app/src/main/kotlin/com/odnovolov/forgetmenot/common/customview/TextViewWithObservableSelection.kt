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
    private var selectedRange = SelectedRange(0, 0)
        set(value) {
            if (field != value) {
                field = value
                selectedRangeObserver?.invoke(field.startIndex, field.endIndex)
                selectedTextObserver?.invoke(selectedText())
            }
        }

    private var selectedRangeObserver: ((startIndex: Int, endIndex: Int) -> Unit)? = null
        set(value) {
            field = value
            field?.invoke(selectedRange.startIndex, selectedRange.endIndex)
        }

    private var selectedTextObserver: ((String) -> Unit)? = null
        set(value) {
            field = value
            field?.invoke(selectedText())
        }

    private fun selectedText(): String {
        return if (text.isNullOrEmpty()) ""
        else text.toString().substring(selectedRange.startIndex, selectedRange.endIndex)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        val startIndex = minOf(selStart, selEnd)
        val endIndex = maxOf(selStart, selEnd)
        selectedRange = SelectedRange(startIndex, endIndex)
    }

    fun observeSelectedRange(observer: ((startIndex: Int, endIndex: Int) -> Unit)?) {
        this.selectedRangeObserver = observer
    }

    fun observeSelectedText(observer: ((String) -> Unit)?) {
        this.selectedTextObserver = observer
    }

    private class SelectedRange(val startIndex: Int, val endIndex: Int)
}