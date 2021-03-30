package com.odnovolov.forgetmenot.presentation.common.customview.undoredoedittext

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class UndoRedoEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(
    context,
    attrs
) {
    var undoStack: UndoStack = UndoStack()
    var redoStack: UndoStack = UndoStack()
    var onUndoRedoChangedListener: OnUndoRedoChangedListener? = null

    init {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                doBeforeTextChanged(s, start, count, after)
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                doOnTextChanged(text, start, before, count)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        addTextChangedListener(textWatcher)
    }

    protected var isDoingUndoRedo = false

    private var textLastChange: TextChange? = null

    private fun doBeforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
        if (!isDoingUndoRedo && onUndoRedoChangedListener != null) {
            textLastChange = if (count < UndoStack.MAX_SIZE) {
                TextChange(
                    newText = "",
                    oldText = text?.subSequence(start, start + count).toString(),
                    start = start
                )
            } else {
                undoStack.removeAll()
                redoStack.removeAll()
                null
            }
        }
    }

    private fun doOnTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        if (!isDoingUndoRedo && textLastChange != null && onUndoRedoChangedListener != null) {
            if (count < UndoStack.MAX_SIZE) {
                textLastChange?.newText = text?.subSequence(start, start + count).toString()
                if (start == textLastChange?.start &&
                    (textLastChange?.oldText?.isNotEmpty()!! || textLastChange?.newText?.isNotEmpty()!!) &&
                    textLastChange?.oldText != textLastChange?.newText) {
                    undoStack.push(textLastChange!!)
                    redoStack.removeAll()
                }
            } else {
                undoStack.removeAll()
                redoStack.removeAll()
            }
            textLastChange = null
            onUndoRedoChangedListener?.onUndoRedoChanged()
        }
    }

    fun canUndo(): Boolean = undoStack.canUndo()
    fun canRedo(): Boolean = redoStack.canUndo()

    fun undo() {
        val text = text ?: return
        val textChange = undoStack.pop()
        if (textChange.start >= 0) {
            isDoingUndoRedo = true
            if (textChange.start > text.length) {
                textChange.start = text.length
            }
            var end = textChange.start + textChange.newText.length
            if (end < 0) {
                end = 0
            }
            if (end > text.length) {
                end = text.length
            }
            redoStack.push(textChange)
            text.replace(textChange.start, end, textChange.oldText)
            setSelection(textChange.start + textChange.oldText.length)
            isDoingUndoRedo = false
        } else {
            undoStack.removeAll()
        }
        onUndoRedoChangedListener?.onUndoRedoChanged()
    }

    fun redo() {
        val textChange = redoStack.pop()
        if (textChange.start >= 0) {
            isDoingUndoRedo = true
            undoStack.push(textChange)
            text?.replace(
                textChange.start,
                textChange.start + textChange.oldText.length,
                textChange.newText
            )
            setSelection(textChange.start + textChange.newText.length)
            isDoingUndoRedo = false
        } else {
            undoStack.removeAll()
        }
        onUndoRedoChangedListener?.onUndoRedoChanged()
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                state.undoStackState?.let(undoStack::restoreFromSerializableString)
                state.redoStackState?.let(redoStack::restoreFromSerializableString)
                onUndoRedoChangedListener?.onUndoRedoChanged()
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            undoStackState = undoStack.toSerializedString()
            redoStackState = redoStack.toSerializedString()
        }
    }

    class SavedState : BaseSavedState {
        var undoStackState: String? = null
        var redoStackState: String? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            val bundle = source.readBundle(javaClass.classLoader)
            undoStackState = bundle?.getString(UNDO_STACK_STATE)
            redoStackState = bundle?.getString(REDO_STACK_STATE)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            val bundle = Bundle()
            bundle.putString(UNDO_STACK_STATE, undoStackState)
            bundle.putString(REDO_STACK_STATE, redoStackState)
            out.writeBundle(bundle)
        }

        companion object {
            @Suppress("UNUSED")
            @JvmField
            val CREATOR = object : Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }

            private const val UNDO_STACK_STATE = "UNDO_STACK_STATE"
            private const val REDO_STACK_STATE = "REDO_STACK_STATE"
        }
    }

    fun interface OnUndoRedoChangedListener {
        fun onUndoRedoChanged()
    }
}