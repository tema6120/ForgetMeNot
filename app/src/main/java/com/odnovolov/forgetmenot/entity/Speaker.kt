package com.odnovolov.forgetmenot.entity

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import java.util.*
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.lifecycle.MutableLiveData

class Speaker(val context: Context) {
    private val availableLanguagesInternal = MutableLiveData<List<Locale>>()
    val availableLanguages: LiveData<List<Locale>> = availableLanguagesInternal

    private val tts = TextToSpeech(context, TextToSpeech.OnInitListener { status: Int ->
        if (status == TextToSpeech.SUCCESS) {
            val availableLanguages = getAvailableLanguages()
            availableLanguagesInternal.value = availableLanguages
        } else {
            Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_LONG)
                .show()
        }
    })

    private fun getAvailableLanguages(): List<Locale> {
        return if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            tts.availableLanguages.toList()
        } else {
            getAvailableLanguagesLegacy()
        }
    }

    private fun getAvailableLanguagesLegacy(): List<Locale> {
        val availableLanguages = ArrayList<Locale>()
        for (locale in Locale.getAvailableLocales()) {
            val hasCountry = locale.country.isNotEmpty()
            val hasVariant = locale.variant.isNotEmpty()

            val isLocaleSupported = when (tts.isLanguageAvailable(locale)) {
                TextToSpeech.LANG_AVAILABLE -> !hasCountry && !hasVariant
                TextToSpeech.LANG_COUNTRY_AVAILABLE -> hasCountry && !hasVariant
                TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
                else -> false
            }
            if (isLocaleSupported) {
                availableLanguages.add(locale)
            }
        }
        return availableLanguages
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}