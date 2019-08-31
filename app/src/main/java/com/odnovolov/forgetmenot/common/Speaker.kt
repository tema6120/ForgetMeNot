package com.odnovolov.forgetmenot.common

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class Speaker(context: Context, onInit: () -> Unit) {
    private val tts = TextToSpeech(context, TextToSpeech.OnInitListener { status: Int ->
        if (status == TextToSpeech.SUCCESS) {
            onInit()
        } else {
            Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_LONG)
                .show()
        }
    })

    val availableLanguages: Set<Locale>
        get() = tts.availableLanguages

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}