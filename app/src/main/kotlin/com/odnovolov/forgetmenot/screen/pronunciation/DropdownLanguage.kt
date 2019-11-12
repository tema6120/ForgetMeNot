package com.odnovolov.forgetmenot.screen.pronunciation

import java.util.*

data class DropdownLanguage(
    // null means default language
    val language: Locale?,

    // we use 'isSelected' for each language to take advantages of ListAdapter
    val isSelected: Boolean
)