package com.odnovolov.forgetmenot.screen.home.adddeck

import com.odnovolov.forgetmenot.screen.home.adddeck.AddDeckEvent.ParsingFinishedWithError
import com.odnovolov.forgetmenot.screen.home.adddeck.AddDeckEvent.ParsingFinishedWithSuccess
import com.odnovolov.forgetmenot.screen.home.adddeck.Parser.IllegalCardFormatException
import kotlinx.coroutines.launch
import java.io.InputStream
import java.nio.charset.Charset

fun AddDeckController.launchParsing(inputStream: InputStream) = launch {
    val resultEvent = try {
        val cardPrototypes = Parser.parse(inputStream, Charset.defaultCharset())
        ParsingFinishedWithSuccess(cardPrototypes)
    } catch (e: IllegalCardFormatException) {
        ParsingFinishedWithError(e)
    }
    dispatchSafely(resultEvent)
}
