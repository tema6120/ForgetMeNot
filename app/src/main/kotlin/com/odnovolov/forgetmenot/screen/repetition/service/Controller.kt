package com.odnovolov.forgetmenot.screen.repetition.service

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.entity.SpeakEvent
import com.odnovolov.forgetmenot.common.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.screen.exercise.TextInBracketsRemover
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceEvent.*
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceOrder.Speak
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class RepetitionServiceController :
    BaseController<RepetitionServiceEvent, RepetitionServiceOrder>() {
    private val queries: RepetitionServiceControllerQueries =
        database.repetitionServiceControllerQueries
    private val textInBracketsRemover by lazy { TextInBracketsRemover() }

    init {
        dispatch(Init)
    }

    override fun handleEvent(event: RepetitionServiceEvent) {
        when (event) {
            Init -> {
                startRepetition()
            }

            SpeakingFinished -> {
                executeNextSpeakEvent()
            }

            DelayIsUp -> {
                executeNextSpeakEvent()
            }
        }
    }

    private fun startRepetition() {
        if (!queries.isPlaying().executeAsOne()) return
        val speakEvent: SpeakEvent? = queries.getCurrentSpeakEvent().executeAsOneOrNull()
        checkAndExecuteSpeakEvent(speakEvent)
    }

    private fun executeNextSpeakEvent() {
        if (!queries.isPlaying().executeAsOne()) return
        val speakEvent: SpeakEvent? = queries.getNextSpeakEvent().executeAsOneOrNull()
        checkAndExecuteSpeakEvent(speakEvent)
    }

    private fun checkAndExecuteSpeakEvent(speakEvent: SpeakEvent?) {
        if (speakEvent != null) {
            queries.incrementSpeakEventOrdinal()
            executeSpeakEvent(speakEvent)
        } else {
            val nextRepetitionCardId: Long? =
                queries.getNextRepetitionCardId().executeAsOne().nextRepetitionCardId
            if (nextRepetitionCardId == null) {
                queries.setIsPlaying(false)
            } else {
                queries.updateRepetitionState(
                    currentRepetitionCardId = nextRepetitionCardId,
                    speakEventOrdinal = 1
                )
                val newSpeakEvent = queries.getCurrentSpeakEvent().executeAsOne()
                executeSpeakEvent(newSpeakEvent)
            }
        }
    }

    private fun executeSpeakEvent(speakEvent: SpeakEvent) {
        when (speakEvent) {
            SpeakQuestion -> {
                queries.speakingDataForQuestion()
                    .executeAsOne()
                    .run { speak(text, language, doNotSpeakTextInBrackets) }
            }
            SpeakAnswer -> {
                queries.speakingDataForAnswer()
                    .executeAsOne()
                    .run { speak(text, language, doNotSpeakTextInBrackets) }
            }
            is Delay -> {
                launch {
                    delay(speakEvent.seconds * 1000L)
                    dispatch(DelayIsUp)
                }
            }
        }
    }

    private fun speak(text: String, language: Locale?, doNotSpeakTextInBrackets: Boolean) {
        val textToSpeak =
            if (doNotSpeakTextInBrackets) textInBracketsRemover.process(text)
            else text
        issueOrder(Speak(textToSpeak, language))
    }
}