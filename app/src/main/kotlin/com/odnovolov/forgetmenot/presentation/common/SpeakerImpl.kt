package com.odnovolov.forgetmenot.presentation.common

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.TtsInitializationFailed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.util.*

class SpeakerImpl(applicationContext: Context) : Speaker {
    class State : FlowableState<State>() {
        var isInitialized: Boolean by me(false)
        var availableLanguages: Set<Locale> by me(emptySet())
    }

    sealed class Event {
        object TtsInitializationFailed : Event()
    }

    val state = State()
    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private var defaultLanguage: Locale? = null
    private var delayedSpokenText: String? = null
    private var delayedLanguage: Locale? = null
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

    private val initListener = TextToSpeech.OnInitListener { status: Int ->
        coroutineScope.launch {
            if (status == TextToSpeech.SUCCESS) {
                state.isInitialized = true
                setDefaultLanguage()
                updateAvailableLanguages()
                speakDelayedTextIfExists()
            } else {
                eventFlow.send(TtsInitializationFailed)
            }
        }
    }

    private val tts: TextToSpeech = TextToSpeech(applicationContext, initListener)

    private fun setDefaultLanguage() {
        defaultLanguage = try {
            tts.defaultVoice?.locale
        } catch (e: NullPointerException) {
            null
        }
    }

    private fun updateAvailableLanguages() {
        state.availableLanguages = try {
            tts.availableLanguages
        } catch (e: NullPointerException) {
            emptySet()
        }
    }

    private fun speakDelayedTextIfExists() {
        if (delayedSpokenText != null) {
            speak(delayedSpokenText!!, delayedLanguage)
            delayedSpokenText = null
            delayedLanguage = null
        }
    }

    override fun speak(text: String, language: Locale?) {
        coroutineScope.launch {
            if (!state.isInitialized) {
                delayedSpokenText = text
                delayedLanguage = language
            } else {
                currentLanguage = language
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
            }
        }
    }

    override fun setOnSpeakingFinished(onSpeakingFinished: () -> Unit) {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String?) {
                onSpeakingFinished()
            }

            override fun onError(utteranceId: String?) {}

            override fun onStart(utteranceId: String?) {}
        })
    }

    override fun stop() {
        coroutineScope.launch {
            tts.stop()
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
        coroutineScope.cancel()
    }
}