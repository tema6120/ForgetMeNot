package com.odnovolov.forgetmenot.screen.exercise

object Prompter {
    private const val MASK = '■'

    fun maskLetters(answer: String): String {
        return answer
            .map { if (it.isLetter()) MASK else it }
            .joinToString(separator = "")
    }

    fun unmaskFirst(answer: String, hint: String): String {
        val firstMaskIndex = hint.indexOf(MASK)
        if (firstMaskIndex == -1) return answer
        val unmaskedLetter = answer[firstMaskIndex]
        return with(StringBuilder(hint)) {
            setCharAt(firstMaskIndex, unmaskedLetter)
            toString()
        }
    }

    fun unmaskRange(answer: String, hint: String, startIndex: Int, endIndex: Int): String {
        val unmasked = answer.substring(startIndex, endIndex)
        return hint.replaceRange(startIndex, endIndex, unmasked)
    }
}