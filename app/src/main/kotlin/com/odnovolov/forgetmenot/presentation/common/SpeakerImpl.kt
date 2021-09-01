package com.odnovolov.forgetmenot.presentation.common

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.presentation.common.ActivityLifecycleCallbacksInterceptor.ActivityLifecycleEvent
import com.odnovolov.forgetmenot.presentation.common.ActivityLifecycleCallbacksInterceptor.ActivityLifecycleEvent.ActivityPaused
import com.odnovolov.forgetmenot.presentation.common.ActivityLifecycleCallbacksInterceptor.ActivityLifecycleEvent.ActivityResumed
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager.AudiofocusState
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LanguageStatus.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Status.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList

class SpeakerImpl(
    private val applicationContext: Context,
    private val activityLifecycleEvents: Flow<ActivityLifecycleEvent>,
    private val audioFocusManager: AudioFocusManager,
    private val lastUsedLanguages: LastUsedLanguages
) : Speaker {
    class State : FlowMaker<State>() {
        var status: Status by flowMaker(Initialization)
        var ttsEngine: String? by flowMaker(null)
        var defaultLanguage: Locale by flowMaker(DEFAULT_LANGUAGE)
        var availableLanguages: Set<Locale> by flowMaker(emptySet())
        var isPreparingToSpeak: Boolean by flowMaker(false)
        var isSpeaking: Boolean by flowMaker(false)
    }

    class LastUsedLanguages(
        language1: Locale?,
        language2: Locale?
    ) : FlowMakerWithRegistry<LastUsedLanguages>() {
        var language1: Locale? by flowMaker(language1)
        var language2: Locale? by flowMaker(language2)

        override fun copy() = LastUsedLanguages(language1, language2)
    }

    enum class Status {
        Initialization,
        FailedToInitialize,
        Initialized,
        Closed
    }

    sealed class Event {
        object SpeakError : Event()
        object CannotGainAudioFocus : Event()
    }

    enum class LanguageStatus {
        Available,
        NotSupported,
        MissingData
    }

    private data class TtsWrapper(
        val id: Long,
        var tts: TextToSpeech,
        var ttsEngine: String?,
        var language: Locale?
    ) {
        var isInitialized: Boolean = false
        var isSpeaking: Boolean = false
        var languageStatus: LanguageStatus? = null
        var lastUsedAt: Long = System.currentTimeMillis()

        // Sometimes after being in the background some engines returns LANG_NOT_SUPPORTED.
        // To fix it, we try to restart engine but no more that once.
        var hasAttemptToRestartEngineToFixProblems: Boolean = true
    }

    private class SpeakingTask(val text: String, val language: Locale?)

    val state = State()
    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()
    private val speakerThreadContext = newSingleThreadContext("SpeakerThread")
    private val coroutineScope = CoroutineScope(Job() + speakerThreadContext)
    private var isConformedToCurrentTtsEngine = false
    private val ttsPool: MutableList<TtsWrapper> = ArrayList()
    private var needToRestartSpeakingTts = false
    private var isAppBackground = false
    private var speakingTask: SpeakingTask? = null
    private var listeners: MutableList<() -> Unit> = ArrayList()
    private val channelsForObservingLanguageStatus: MutableList<Pair<Locale?, Channel<LanguageStatus?>>> =
        CopyOnWriteArrayList()
    private val toneGenerator: ToneGenerator
            by lazy { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    private var errorSoundJob: Job? = null

    init {
        coroutineScope.launch {
            with(lastUsedLanguages) {
                language1?.let(::registerNewTts)
                language2?.let(::registerNewTts)
            }
            observeActivityLifecycleEvents()
            observeAudioFocusState()
        }
    }

    private fun observeAudioFocusState() {
        audioFocusManager.state.flowOf(AudioFocusManager.State::audioFocusState)
            .observe(coroutineScope) { audioFocusState: AudiofocusState ->
                when (audioFocusState) {
                    AudiofocusState.LOSS,
                    AudiofocusState.LOSS_TRANSIENT -> {
                        if (state.isPreparingToSpeak || state.isSpeaking) {
                            val success = audioFocusManager.request(AUDIOFOCUS_KEY)
                            if (!success) {
                                stopSpeaking()
                                state.isPreparingToSpeak = false
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
    }

    private fun registerNewTts(language: Locale?): TtsWrapper {
        val id: Long = generateId()
        val tts = TextToSpeech(applicationContext) { status: Int -> onTtsInit(id, status) }
        val ttsEngine: String? = tts.defaultEngine
        setTtsEngine(ttsEngine)
        val ttsWrapper = TtsWrapper(id, tts, ttsEngine, language)
        ttsPool.add(ttsWrapper)
        return ttsWrapper
    }

    private fun setTtsEngine(ttsEngine: String?) {
        if (state.ttsEngine == ttsEngine) return
        state.ttsEngine = ttsEngine
        isConformedToCurrentTtsEngine = false
    }

    private fun observeActivityLifecycleEvents() {
        activityLifecycleEvents.observe(coroutineScope) { activityLifecycleEvent ->
            when (activityLifecycleEvent) {
                is ActivityResumed -> {
                    ttsPool.find { ttsWrapper -> ttsWrapper.isInitialized }
                        ?.let { ttsWrapper ->
                            setTtsEngine(ttsWrapper.tts.defaultEngine)
                            if (state.ttsEngine != ttsWrapper.ttsEngine) {
                                restartTtsWithOldTtsEngine()
                            } else {
                                ttsPool.forEach { it.updateLanguageStatus() }
                                updateAvailableLanguages()
                                updateDefaultLanguage()
                            }
                        }
                    isAppBackground = false
                }
                is ActivityPaused -> {
                    isAppBackground = true
                }
            }
        }
    }

    private fun onTtsInit(id: Long, status: Int) {
        coroutineScope.launch {
            val ttsWrapper: TtsWrapper = ttsPool.first { it.id == id }
            val isTtsEngineValid = validateTtsEngine(ttsWrapper)
            if (!isTtsEngineValid) return@launch
            if (status == TextToSpeech.SUCCESS) {
                ttsWrapper.isInitialized = true
                if (!isConformedToCurrentTtsEngine) {
                    conformToCurrentTtsEngine()
                }
                state.status = Initialized
                ttsWrapper.setProgressListener()
                if (ttsWrapper.language != null) {
                    ttsWrapper.updateLanguageStatus()
                }
                tryToExecuteSpeakingTask()
            } else {
                state.status = FailedToInitialize
                state.isPreparingToSpeak = false
                speakingTask = null
            }
        }
    }

    private fun validateTtsEngine(ttsWrapper: TtsWrapper): Boolean {
        setTtsEngine(ttsWrapper.tts.defaultEngine)
        if (ttsWrapper.ttsEngine != state.ttsEngine) {
            ttsWrapper.restartTts()
            return false
        }
        return true
    }

    private fun TtsWrapper.restartTts() {
        tts.shutdown()
        isInitialized = false
        updateLanguageStatus()
        ttsEngine = tts.defaultEngine
        tts = TextToSpeech(applicationContext) { status: Int -> onTtsInit(id, status) }
    }

    private fun conformToCurrentTtsEngine() {
        updateAvailableLanguages()
        updateDefaultLanguage()
        restartTtsWithOldTtsEngine()
        isConformedToCurrentTtsEngine = true
    }

    private fun updateAvailableLanguages() {
        ttsPool.find { ttsWrapper: TtsWrapper ->
            ttsWrapper.isInitialized && ttsWrapper.ttsEngine == state.ttsEngine
        }
            ?.let { ttsWrapper: TtsWrapper ->
                state.availableLanguages = try {
                    ttsWrapper.tts.availableLanguages
                } catch (e: NullPointerException) {
                    emptySet()
                }
            }
    }

    private fun updateDefaultLanguage() {
        ttsPool.find { ttsWrapper: TtsWrapper ->
            ttsWrapper.isInitialized && ttsWrapper.ttsEngine == state.ttsEngine
        }
            ?.let { ttsWrapper: TtsWrapper ->
                state.defaultLanguage = try {
                    ttsWrapper.tts.defaultVoice?.locale
                } catch (e: NullPointerException) {
                    DEFAULT_LANGUAGE
                } ?: DEFAULT_LANGUAGE
            }
        ensureObservationLanguageStatusOfDefaultLanguage()
    }

    private fun ensureObservationLanguageStatusOfDefaultLanguage() {
        val isDefaultLanguageStatusObserved: Boolean =
            channelsForObservingLanguageStatus.any { (language, _) -> language == null }
        if (!isDefaultLanguageStatusObserved) return
        val ttsWrapper: TtsWrapper = obtainTtsWrapper(state.defaultLanguage)
        onLanguageStatusChanged(ttsWrapper)
    }

    private fun restartTtsWithOldTtsEngine() {
        ttsPool.forEach { ttsWrapper: TtsWrapper ->
            if (ttsWrapper.ttsEngine != state.ttsEngine) {
                if (ttsWrapper.isSpeaking) {
                    needToRestartSpeakingTts = true
                } else {
                    ttsWrapper.restartTts()
                }
            }
        }
    }

    private fun TtsWrapper.setProgressListener() {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                coroutineScope.launch {
                    if (!state.isPreparingToSpeak) {
                        tts.stop()
                    } else {
                        state.isPreparingToSpeak = false
                        state.isSpeaking = true
                        isSpeaking = true
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                coroutineScope.launch {
                    state.isSpeaking = false
                    isSpeaking = false
                    listeners.forEach { it.invoke() }
                    if (needToRestartSpeakingTts) {
                        restartTts()
                        needToRestartSpeakingTts = false
                    }
                    speakingTask = null
                    audioFocusManager.abandonRequest(AUDIOFOCUS_KEY)
                }
            }

            override fun onError(utteranceId: String?) {
                coroutineScope.launch {
                    state.isPreparingToSpeak = false
                    state.isSpeaking = false
                    isSpeaking = false
                    listeners.forEach { it.invoke() }
                    speakingTask = null
                    audioFocusManager.abandonRequest(AUDIOFOCUS_KEY)
                }
            }
        })
    }

    override fun speak(text: String, language: Locale?) {
        coroutineScope.launch {
            if (state.status == FailedToInitialize || state.status == Closed) {
                return@launch
            }
            state.isPreparingToSpeak = true
            speakingTask = SpeakingTask(text, language)
            tryToExecuteSpeakingTask()
        }
    }

    private fun tryToExecuteSpeakingTask() {
        if (state.status != Initialized) return
        val speakingTask: SpeakingTask = speakingTask ?: return
        val specifiedLanguage: Locale = speakingTask.language ?: run {
            if (isAppBackground) {
                updateDefaultLanguage()
            }
            state.defaultLanguage
        }
        val ttsWrapper: TtsWrapper = obtainTtsWrapper(specifiedLanguage)
        if (isAppBackground) {
            val isTtsEngineValid = validateTtsEngine(ttsWrapper)
            if (!isTtsEngineValid) return
        }
        if (!ttsWrapper.isInitialized) return
        if (!audioFocusManager.request(AUDIOFOCUS_KEY)) {
            eventFlow.send(CannotGainAudioFocus)
            state.isPreparingToSpeak = false
            return
        }
        stopSpeaking()
        ttsWrapper.speak(speakingTask.text)
        this.speakingTask = null
    }

    private fun obtainTtsWrapper(language: Locale): TtsWrapper {
        return ttsPool.find { ttsWrapper: TtsWrapper -> ttsWrapper.language == language }
            ?: ttsPool.find { ttsWrapper: TtsWrapper -> ttsWrapper.language == null }
                ?.apply { setLanguage(language) }
            ?: if (ttsPool.size < MAX_TTS_INSTANCES) {
                registerNewTts(language)
            } else {
                val observedLanguages: List<Locale> = channelsForObservingLanguageStatus
                    .map { (language: Locale?, _) -> language ?: state.defaultLanguage }
                val lastUsedVacantTtsWrapper: TtsWrapper? =
                    ttsPool.filter { ttsWrapper -> ttsWrapper.language !in observedLanguages }
                        .minByOrNull { ttsWrapper: TtsWrapper -> ttsWrapper.lastUsedAt }
                lastUsedVacantTtsWrapper?.apply { setLanguage(language) }
                    ?: registerNewTts(language)
            }
    }

    private fun TtsWrapper.setLanguage(language: Locale) {
        this.language = language
        updateLanguageStatus()
    }

    private fun TtsWrapper.updateLanguageStatus() {
        languageStatus = if (isInitialized) {
            val setLanguageResultCode: Int = tts.setLanguage(language)
            if (hasAttemptToRestartEngineToFixProblems &&
                (setLanguageResultCode == TextToSpeech.LANG_NOT_SUPPORTED
                        ||
                        setLanguageResultCode == TextToSpeech.LANG_MISSING_DATA)
            ) {
                hasAttemptToRestartEngineToFixProblems = false
                restartTts()
                return
            }
            when (setLanguageResultCode) {
                TextToSpeech.LANG_NOT_SUPPORTED -> NotSupported
                TextToSpeech.LANG_MISSING_DATA -> MissingData
                else -> {
                    hasAttemptToRestartEngineToFixProblems = true
                    Available
                }
            }
        } else {
            null
        }
        onLanguageStatusChanged(this)
    }

    private fun onLanguageStatusChanged(ttsWrapper: TtsWrapper) {
        channelsForObservingLanguageStatus
            .forEach { (language: Locale?, channel: Channel<LanguageStatus?>) ->
                val specifiedLanguage: Locale = language ?: state.defaultLanguage
                if (ttsWrapper.language == specifiedLanguage) {
                    channel.offer(ttsWrapper.languageStatus)
                }
            }
    }

    private fun TtsWrapper.speak(text: String) {
        lastUsedAt = System.currentTimeMillis()
        if (languageStatus != Available) {
            state.isPreparingToSpeak = false
            playErrorSound()
            eventFlow.send(SpeakError)
            audioFocusManager.abandonRequest(AUDIOFOCUS_KEY)
        } else {
            val queueMode: Int = TextToSpeech.QUEUE_FLUSH
            val utteranceId: String = UUID.randomUUID().toString()
            val result: Int = tts.speak(text, queueMode, null, utteranceId)
            if (result == TextToSpeech.ERROR) {
                state.isPreparingToSpeak = false
                playErrorSound()
                eventFlow.send(SpeakError)
                audioFocusManager.abandonRequest(AUDIOFOCUS_KEY)
                updateLanguageStatus()
            } else {
                language?.let(::saveLastUsedLanguage)
            }
        }
    }

    private fun playErrorSound() {
        errorSoundJob = coroutineScope.launch {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, ERROR_SOUND_DURATION)
            delay(ERROR_SOUND_DURATION.toLong())
            if (isActive) {
                listeners.forEach { it.invoke() }
                errorSoundJob = null
            }
        }
    }

    private fun saveLastUsedLanguage(language: Locale) {
        if (lastUsedLanguages.language1 != language) {
            lastUsedLanguages.language2 = lastUsedLanguages.language1
            lastUsedLanguages.language1 = language
        }
    }

    fun languageStatusOf(language: Locale?): Flow<LanguageStatus?> = flow {
        val specifiedLanguage = language ?: state.defaultLanguage
        val ttsWrapper = obtainTtsWrapper(specifiedLanguage)
        emit(ttsWrapper.languageStatus)
        val channel = Channel<LanguageStatus?>(Channel.CONFLATED)
        val languageChannel: Pair<Locale?, Channel<LanguageStatus?>> = language to channel
        channelsForObservingLanguageStatus.add(languageChannel)
        try {
            for (languageStatus: LanguageStatus? in channel) {
                emit(languageStatus)
            }
        } finally {
            channelsForObservingLanguageStatus.remove(languageChannel)
        }
    }
        .distinctUntilChanged()
        .flowOn(speakerThreadContext)

    override fun addOnSpeakingFinishedListener(onSpeakingFinished: () -> Unit) {
        coroutineScope.launch {
            listeners.add(onSpeakingFinished)
        }
    }

    override fun removeOnSpeakingFinishedListener(onSpeakingFinished: () -> Unit) {
        coroutineScope.launch {
            listeners.remove(onSpeakingFinished)
        }
    }

    override fun stop() {
        coroutineScope.launch {
            stopSpeaking()
            state.isPreparingToSpeak = false
            speakingTask = null
            audioFocusManager.abandonRequest(AUDIOFOCUS_KEY)
        }
    }

    private fun stopSpeaking() {
        if (!state.isSpeaking) return
        ttsPool.find { ttsWrapper: TtsWrapper -> ttsWrapper.isSpeaking }
            ?.let { ttsWrapper: TtsWrapper ->
                ttsWrapper.tts.stop()
                ttsWrapper.isSpeaking = false
            }
        state.isSpeaking = false
        errorSoundJob?.cancel()
        errorSoundJob = null
    }

    fun shutdown() {
        coroutineScope.launch {
            stopSpeaking()
            speakingTask = null
            ttsPool.forEach { it.tts.shutdown() }
            ttsPool.clear()
            with(state) {
                status = Closed
                availableLanguages = emptySet()
                isPreparingToSpeak = false
            }
            audioFocusManager.abandonRequest(AUDIOFOCUS_KEY)
            coroutineScope.cancel()
        }
    }

    private companion object {
        val DEFAULT_LANGUAGE: Locale = Locale.ENGLISH
        const val ERROR_SOUND_DURATION = 500
        const val MAX_TTS_INSTANCES: Int = 4
        const val AUDIOFOCUS_KEY = "SpeakerImpl"
    }
}