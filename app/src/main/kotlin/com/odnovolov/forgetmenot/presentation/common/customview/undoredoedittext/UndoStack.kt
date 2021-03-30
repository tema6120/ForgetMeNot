package com.odnovolov.forgetmenot.presentation.common.customview.undoredoedittext

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class UndoStack() {
    companion object {
        const val MAX_SIZE = Integer.MAX_VALUE
    }

    val size: Int
        get() = state.stack.size

    @Serializable
    private data class State(
        var stack: MutableList<TextChange> = mutableListOf(),
        var currentSize: Int = 0
    )

    private var state = State()

    private constructor(stack: List<TextChange>) : this() {
        state.stack = stack.toMutableList()
    }

    operator fun get(index: Int): TextChange {
        return state.stack[index]
    }

    fun pop(): TextChange {
        val item = state.stack[size - 1]
        state.stack.removeAt(size - 1)
        state.currentSize -= item.newText.length + item.oldText.length
        return item
    }

    fun push(textChange: TextChange) {
        val delta = textChange.newText.length + textChange.oldText.length
        if (delta < MAX_SIZE) {
            if (size > 0) {
                val previous = state.stack[size - 1]
                val toCharArray: CharArray
                val length: Int
                var allWhitespace: Boolean
                var allLettersDigits: Boolean
                var i = 0
                if (textChange.oldText.isEmpty() &&
                    textChange.newText.length == 1 &&
                    previous.oldText.isEmpty()) {
                    if (previous.start + previous.newText.length != textChange.start) {
                        state.stack.add(textChange)
                    } else if (textChange.newText[0].isWhitespace()) {
                        allWhitespace = true
                        toCharArray = previous.newText.toCharArray()
                        length = toCharArray.size
                        while (i < length) {
                            if (!toCharArray[i].isWhitespace()) {
                                allWhitespace = false
                            }
                            i++
                        }
                        if (allWhitespace) {
                            previous.newText += textChange.newText
                        } else {
                            state.stack.add(textChange)
                        }
                    } else if (textChange.newText[0].isLetterOrDigit()) {
                        allLettersDigits = true
                        toCharArray = previous.newText.toCharArray()
                        length = toCharArray.size
                        while (i < length) {
                            if (!toCharArray[i].isLetterOrDigit()) {
                                allLettersDigits = false
                            }
                            i++
                        }
                        if (allLettersDigits) {
                            previous.newText += textChange.newText
                        } else {
                            state.stack.add(textChange)
                        }
                    } else {
                        state.stack.add(textChange)
                    }
                } else if (textChange.oldText.length != 1 ||
                    textChange.newText.isNotEmpty() ||
                    previous.newText.isNotEmpty()) {
                    state.stack.add(textChange)
                } else if (previous.start - 1 != textChange.start) {
                    state.stack.add(textChange)
                } else if (textChange.oldText[0].isWhitespace()) {
                    allWhitespace = true
                    toCharArray = previous.oldText.toCharArray()
                    length = toCharArray.size
                    while (i < length) {
                        if (!toCharArray[i].isWhitespace()) {
                            allWhitespace = false
                        }
                        i++
                    }
                    if (allWhitespace) {
                        previous.oldText = textChange.oldText + previous.oldText
                        previous.start -= textChange.oldText.length
                    } else {
                        state.stack.add(textChange)
                    }
                } else if (textChange.oldText[0].isLetterOrDigit()) {
                    allLettersDigits = true
                    toCharArray = previous.oldText.toCharArray()
                    length = toCharArray.size
                    while (i < length) {
                        if (!toCharArray[i].isLetterOrDigit()) {
                            allLettersDigits = false
                        }
                        i++
                    }
                    if (allLettersDigits) {
                        previous.oldText = textChange.oldText + previous.oldText
                        previous.start -= textChange.oldText.length
                    } else {
                        state.stack.add(textChange)
                    }
                } else {
                    state.stack.add(textChange)
                }
            } else {
                state.stack.add(textChange)
            }
            state.currentSize += delta
            while (state.currentSize > MAX_SIZE) {
                if (!removeLast()) {
                    return
                }
            }
            return
        }
        removeAll()
    }

    fun removeAll() {
        state.currentSize = 0
        state.stack.clear()
    }

    fun canUndo(): Boolean {
        return size > 0
    }

    fun clone(): UndoStack {
        return UndoStack(state.stack)
    }

    private fun removeLast(): Boolean {
        if (size <= 0) {
            return false
        }
        val item = state.stack[0]
        state.stack.removeAt(0)
        state.currentSize -= item.newText.length + item.oldText.length
        return true
    }

    fun restoreFromSerializableString(serializableString: String) {
        state = Json.Default.decodeFromString(State.serializer(), serializableString)
    }

    fun toSerializedString(): String {
        return Json.Default.encodeToString(State.serializer(), state)
    }
}