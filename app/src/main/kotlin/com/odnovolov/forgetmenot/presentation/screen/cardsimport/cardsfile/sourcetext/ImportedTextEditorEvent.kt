package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext

import java.nio.charset.Charset

sealed class ImportedTextEditorEvent {
    class EncodingWasSelected(val newEncoding: Charset) : ImportedTextEditorEvent()
}