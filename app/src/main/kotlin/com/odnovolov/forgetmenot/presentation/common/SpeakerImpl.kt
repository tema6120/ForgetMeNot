package com.odnovolov.forgetmenot.presentation.common

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.presentation.common.ActivityLifecycleCallbacksInterceptor.ActivityLifecycleEvent
import com.odnovolov.forgetmenot.presentation.common.ActivityLifecycleCallbacksInterceptor.ActivityLifecycleEvent.ActivityResumed
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.TtsInitializationFailed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.*

class SpeakerImpl(
    private val applicationContext: Context,
    private val activityLifecycleEvents: Flow<ActivityLifecycleEvent>
) : Speaker {
    class State : FlowableState<State>() {
        var isInitialized: Boolean by me(false)
        var availableLanguages: Set<Locale> by me(emptySet())
        var isSpeaking: Boolean by me(false)
    }

    sealed class Event {
        object TtsInitializationFailed : Event()
    }

    val state = State()
    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()
    private val coroutineScope = CoroutineScope(newSingleThreadContext("SpeakerThread"))
    private var defaultLanguage: Locale? = null
    private var delayedSpokenText: String? = null
    private var currentTtsEngine: String? = null
    private var delayedLanguage: Locale? = null
    private var onSpeakingFinishedListener: (() -> Unit)? = null
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
            state.isInitialized = true
            currentTtsEngine = tts.defaultEngine
            if (status == TextToSpeech.SUCCESS) {
                setDefaultLanguage()
                updateAvailableLanguages()
                setProgressListener()
                speakDelayedTextIfExists()
            } else {
                eventFlow.send(TtsInitializationFailed)
            }
        }
    }

    private lateinit var tts: TextToSpeech

    init {
        coroutineScope.launch {
            initTts()
            observeActivityLifecycleEvents()
        }
    }

    private fun initTts() {
        state.isInitialized = false
        tts = TextToSpeech(applicationContext, initListener)
    }

    private fun observeActivityLifecycleEvents() {
        activityLifecycleEvents.observe(coroutineScope) { activityLifecycleEvent ->
            if (activityLifecycleEvent is ActivityResumed && isTtsEngineChanged()) {
                restartTts()
            }
        }
    }

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

    private fun setProgressListener() {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                coroutineScope.launch {
                    state.isSpeaking = true
                }
            }

            override fun onDone(utteranceId: String?) {
                coroutineScope.launch {
                    state.isSpeaking = false
                    onSpeakingFinishedListener?.invoke()
                }
            }

            override fun onError(utteranceId: String?) {
                coroutineScope.launch {
                    state.isSpeaking = false
                }
            }
        })
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
            when {
                !state.isInitialized -> {
                    delayedSpokenText = text
                    delayedLanguage = language
                }
                isTtsEngineChanged() -> {
                    delayedSpokenText = text
                    delayedLanguage = language
                    restartTts()
                }
                else -> {
                    currentLanguage = language
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
                }
            }
        }
    }

    override fun setOnSpeakingFinished(onSpeakingFinished: () -> Unit) {
        coroutineScope.launch {
            onSpeakingFinishedListener = onSpeakingFinished
        }
    }

    override fun stop() {
        coroutineScope.launch {
            tts.stop()
            state.isSpeaking = false
        }
    }

    private fun isTtsEngineChanged() = state.isInitialized && currentTtsEngine != tts.defaultEngine

    private fun restartTts() {
        tts.stop()
        state.isSpeaking = false
        tts.shutdown()
        initTts()
    }

    fun shutdown() {
        coroutineScope.launch {
            tts.stop()
            state.isSpeaking = false
            tts.shutdown()
            state.isInitialized = false
            state.availableLanguages = emptySet()
            coroutineScope.cancel()
        }
    }
}