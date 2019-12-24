package com.odnovolov.forgetmenot.common

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.lang.NullPointerException
import java.util.*

class Speaker(context: Context, onInit: () -> Unit = {}) {
    private var defaultLanguage: Locale? = null
    private var isInitialized = false
    private val initListener = TextToSpeech.OnInitListener { status: Int ->
        if (status == TextToSpeech.SUCCESS) {
            defaultLanguage = try {
                tts.defaultVoice?.locale
            } catch (e: NullPointerException) {
                null
            }
            onInit()
            isInitialized = true
            if (delayedSpokenText != null) {
                speak(delayedSpokenText!!, delayedLanguage)
                delayedSpokenText = null
                delayedLanguage = null
            }
        } else {
            Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_LONG)
                .show()
        }
    }
    private val tts: TextToSpeech = TextToSpeech(context, initListener)
    private var currentLanguage: Locale? = null
        set(value) {
            if (value == null && currentLanguage != defaultLanguage) {
                tts.language = defaultLanguage
                field = defaultLanguage
            } else if (value != null && currentLanguage != value) {
                tts.language = value
                field = value
            }
        }

    val availableLanguages: Set<Locale>
        get() {
            return try {
                tts.availableLanguages
            } catch (e: NullPointerException) {
                emptySet()
            }
        }

    private var delayedSpokenText: String? = null
    private var delayedLanguage: Locale? = null

    fun speak(text: String, language: Locale?) {
        if (!isInitialized) {
            delayedSpokenText = text
            delayedLanguage = language
            return
        }
        currentLanguage = language
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}