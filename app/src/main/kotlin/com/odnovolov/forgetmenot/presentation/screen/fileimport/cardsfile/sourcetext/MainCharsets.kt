package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext

import java.nio.charset.Charset

val mainCharsets = listOf<Charset>(
    Charset.forName("UTF-8"),
    Charset.forName("UTF-16LE"),
    Charset.forName("UTF-16BE"),
    Charset.forName("windows-1252"),
    Charset.forName("ISO-8859-1"),
    Charset.forName("ISO-8859-3"),
    Charset.forName("ISO-8859-15"),
    Charset.forName("windows-1256"),
    Charset.forName("ISO-8859-6"),
    Charset.forName("windows-1257"),
    Charset.forName("ISO-8859-4"),
    Charset.forName("windows-1250"),
    Charset.forName("ISO-8859-2"),
    Charset.forName("windows-1251"),
    Charset.forName("ISO-8859-5"),
    Charset.forName("KOI8-R"),
    Charset.forName("KOI8-U"),
    Charset.forName("ISO-8859-13"),
    Charset.forName("windows-1253"),
    Charset.forName("ISO-8859-7"),
    Charset.forName("windows-1255"),
    Charset.forName("ISO-8859-8"),
    Charset.forName("windows-1254"),
    Charset.forName("ISO-8859-9"),
    Charset.forName("windows-1258")
)

val Charset.clarifyingName: String
    get() {
        val group: String? = when (name()) {
            "windows-1252" -> "Western"
            "ISO-8859-1" -> "Western"
            "ISO-8859-3" -> "Western"
            "ISO-8859-15" -> "Western"
            "windows-1256" -> "Arabic"
            "ISO-8859-6" -> "Arabic"
            "windows-1257" -> "Baltic"
            "ISO-8859-4" -> "Baltic"
            "windows-1250" -> "Central European"
            "ISO-8859-2" -> "Central European"
            "windows-1251" -> "Cyrillic"
            "ISO-8859-5" -> "Cyrillic"
            "KOI8-R" -> "Cyrillic"
            "KOI8-U" -> "Cyrillic"
            "ISO-8859-13" -> "Estonian"
            "windows-1253" -> "Greek"
            "ISO-8859-7" -> "Greek"
            "windows-1255" -> "Hebrew"
            "ISO-8859-8" -> "Hebrew"
            "windows-1254" -> "Turkish"
            "ISO-8859-9" -> "Turkish"
            "windows-1258" -> "Vietnamese"
            else -> null
        }
        return if (group == null) name() else "$group (${name()})"
    }