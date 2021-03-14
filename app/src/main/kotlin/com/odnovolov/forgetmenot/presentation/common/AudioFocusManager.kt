package com.odnovolov.forgetmenot.presentation.common

import android.content.Context
import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AudioFocusManager(
    context: Context
) {
    class State : FlowMaker<State>() {
        var audioFocusState: AudiofocusState by flowMaker(AudiofocusState.NONE)
    }

    enum class AudiofocusState {
        NONE,
        GAIN,
        LOSS,
        LOSS_TRANSIENT
    }

    val state = State()
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val afChangeListener by lazy {
        AudioManager.OnAudioFocusChangeListener { focusChange: Int ->
            state.audioFocusState = when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> AudiofocusState.GAIN
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> AudiofocusState.LOSS_TRANSIENT
                else -> AudiofocusState.LOSS
            }
        }
    }
    private var focusRequest: AudioFocusRequestCompat? = null
    private val keys = mutableSetOf<String>()

    fun request(key: String): Boolean {
        return runBlocking(Dispatchers.Main) {
            when (state.audioFocusState) {
                AudiofocusState.GAIN -> {
                    keys.add(key)
                    true
                }
                AudiofocusState.LOSS_TRANSIENT -> {
                    false
                }
                AudiofocusState.NONE,
                AudiofocusState.LOSS -> {
                    val success = requestInternal()
                    if (success) {
                        state.audioFocusState = AudiofocusState.GAIN
                        keys.add(key)
                    }
                    success
                }
            }
        }
    }

    private fun requestInternal(): Boolean {
        val focusRequest = AudioFocusRequestCompat.Builder(
            AudioManagerCompat.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        )
            .setOnAudioFocusChangeListener(afChangeListener)
            .setAudioAttributes(
                AudioAttributesCompat.Builder()
                    .setUsage(AudioAttributesCompat.USAGE_ASSISTANT)
                    .setContentType(AudioAttributesCompat.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .build()
        val success = AudioManagerCompat.requestAudioFocus(
            audioManager,
            focusRequest!!
        ) == AudioManager.AUDIOFOCUS_GAIN
        this.focusRequest = if (success) {
            focusRequest
        } else {
            null
        }
        return success
    }

    fun abandonRequest(key: String) {
        keys.remove(key)
        if (keys.isEmpty()) {
            state.audioFocusState = AudiofocusState.LOSS
            focusRequest?.let { focusRequest ->
                AudioManagerCompat.abandonAudioFocusRequest(audioManager, focusRequest)
            }
            focusRequest = null
        }
    }
}