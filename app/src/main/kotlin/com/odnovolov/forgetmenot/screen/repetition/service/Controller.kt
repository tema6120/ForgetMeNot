package com.odnovolov.forgetmenot.screen.repetition.service

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.screen.exercise.TextInBracketsRemover
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceEvent.*
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceOrder.Speak
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceOrder.StopSpeaking
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class RepetitionServiceController :
    BaseController<RepetitionServiceEvent, RepetitionServiceOrder>() {
    private val queries: RepetitionServiceControllerQueries =
        database.repetitionServiceControllerQueries
    private val textInBracketsRemover by lazy { TextInBracketsRemover() }
    private var delayJob: Job? = null

    init {
        dispatch(Init)
    }

    override fun handleEvent(event: RepetitionServiceEvent) {
        when (event) {
            Init -> {
                if (!queries.isPlaying().executeAsOne()) return
                executeSpeakEvent()
            }

            SpeakingFinished -> {
                tryToExecuteNextSpeakEvent()
            }

            DelayIsUp -> {
                tryToExecuteNextSpeakEvent()
            }

            PauseClicked -> {
                delayJob?.cancel()
                queries.setIsPlaying(false)
                issueOrder(StopSpeaking)
            }

            ResumeClicked -> {
                queries.setIsPlaying(true)
                executeSpeakEvent()
            }
        }
    }

    private fun tryToExecuteNextSpeakEvent() {
        if (!queries.isPlaying().executeAsOne()) return
        val success = tryNext()
        if (success) {
            executeSpeakEvent()
        } else {
            queries.setIsPlaying(false)
        }
    }

    private fun tryNext(): Boolean {
        if (queries.isThereOneMoreSpeakEventForCurrentRepetitionCard().executeAsOne()) {
            queries.incrementSpeakEventOrdinal()
            return true
        }
        val nextRepetitionCardId: Long? = queries.getNextRepetitionCardId().executeAsOne()
            .nextRepetitionCardId
        if (nextRepetitionCardId != null) {
            queries.updateRepetitionState(
                currentRepetitionCardId = nextRepetitionCardId,
                speakEventOrdinal = 1
            )
            return true
        } else {
            return false
        }
    }

    private fun executeSpeakEvent() {
        val speakEvent = queries.getCurrentSpeakEvent().executeAsOne()
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
                delayJob = launch {
                    delay(speakEvent.seconds * 1000L)
                    dispatchSafely(DelayIsUp)
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