package com.odnovolov.forgetmenot.entity

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

class Speaker(val context: Context) {
    private val availableLanguagesInternal = MutableLiveData<List<Locale>>()
    val availableLanguages: LiveData<List<Locale>> = availableLanguagesInternal

    private val tts = TextToSpeech(context, TextToSpeech.OnInitListener { status: Int ->
        if (status == TextToSpeech.SUCCESS) {
            availableLanguagesInternal.value = getAvailableLanguages()
        } else {
            Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_LONG)
                .show()
        }
    })

    private fun getAvailableLanguages(): List<Locale> {
        return tts.availableLanguages.toList()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}