package com.odnovolov.forgetmenot.presentation.screen.fileimport.editor

import android.graphics.Color
import com.brackeys.ui.editorkit.model.ColorScheme
import com.brackeys.ui.language.base.model.SyntaxScheme

val editorColorScheme = ColorScheme(
    textColor = Color.parseColor("#4C4C4C"),
    backgroundColor = Color.parseColor("#FFFFFF"),
    gutterColor = Color.parseColor("#FFFFFF"),
    gutterDividerColor = Color.parseColor("#E1E1E1"),
    gutterCurrentLineNumberColor = Color.parseColor("#6B6B6B"),
    gutterTextColor = Color.parseColor("#C0C0C0"),
    selectedLineColor = Color.parseColor("#F6F6F6"),
    selectionColor = Color.parseColor("#63FF8C00"),
    suggestionQueryColor = Color.parseColor("#000000"),
    findResultBackgroundColor = Color.parseColor("#000000"),
    delimiterBackgroundColor = Color.parseColor("#FF8C00"),
    syntaxScheme = SyntaxScheme(
        numberColor = Color.parseColor("#000000"),
        operatorColor = Color.parseColor("#8369C2"), // question text
        keywordColor = Color.parseColor("#5E8DCB"), // answer text
        typeColor = Color.parseColor("#000000"),
        langConstColor = Color.parseColor("#000000"),
        preprocessorColor = Color.parseColor("#000000"),
        methodColor = Color.parseColor("#000000"),
        stringColor = Color.parseColor("#000000"),
        commentColor = Color.parseColor("#000000"),
        tagColor = Color.parseColor("#000000"),
        tagNameColor = Color.parseColor("#000000"),
        attrNameColor = Color.parseColor("#000000"),
        attrValueColor = Color.parseColor("#000000"),
        entityRefColor = Color.parseColor("#000000")
    )
)