package com.odnovolov.forgetmenot.presentation.screen.cardsimport.editor

import android.content.Context
import androidx.core.content.ContextCompat
import com.brackeys.ui.editorkit.model.ColorScheme
import com.brackeys.ui.language.base.model.SyntaxScheme
import com.odnovolov.forgetmenot.R

fun getEditorColorScheme(context: Context) = ColorScheme(
    textColor = ContextCompat.getColor(context, R.color.esc_text),
    backgroundColor = ContextCompat.getColor(context, R.color.esc_background),
    gutterColor = ContextCompat.getColor(context, R.color.esc_gutter),
    gutterDividerColor = ContextCompat.getColor(context, R.color.esc_gutter_divider),
    gutterCurrentLineNumberColor = ContextCompat.getColor(context, R.color.esc_gutter_current_line_number),
    gutterTextColor = ContextCompat.getColor(context, R.color.esc_gutter_text),
    selectedLineColor = ContextCompat.getColor(context, R.color.esc_selected_line),
    selectionColor = ContextCompat.getColor(context, R.color.esc_selection),
    suggestionQueryColor = -1,
    findResultBackgroundColor = -1,
    delimiterBackgroundColor = ContextCompat.getColor(context, R.color.esc_delimiter_background),
    syntaxScheme = SyntaxScheme(
        numberColor = -1,
        operatorColor = ContextCompat.getColor(context, R.color.esc_question_text),
        keywordColor = ContextCompat.getColor(context, R.color.esc_answer_text),
        typeColor = -1,
        langConstColor = -1,
        preprocessorColor = -1,
        methodColor = -1,
        stringColor = -1,
        commentColor = -1,
        tagColor = -1,
        tagNameColor = -1,
        attrNameColor = -1,
        attrValueColor = -1,
        entityRefColor = -1
    )
)