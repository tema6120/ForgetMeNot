package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext

import java.nio.charset.Charset

sealed class ImportedTextEditorEvent {
    class EncodingIsChanged(val newEncoding: Charset) : ImportedTextEditorEvent()
}