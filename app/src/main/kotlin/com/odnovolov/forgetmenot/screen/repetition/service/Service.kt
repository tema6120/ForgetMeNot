package com.odnovolov.forgetmenot.screen.repetition.service

import android.content.Intent
import android.os.IBinder
import com.odnovolov.forgetmenot.common.Speaker
import com.odnovolov.forgetmenot.common.base.BaseService
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceEvent.SpeakingFinished
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceOrder.Speak

class RepetitionService : BaseService() {
    private val controller = RepetitionServiceController()
    private lateinit var speaker: Speaker

    override fun onCreate() {
        speaker = Speaker(this)
        speaker.setOnSpeakingFinished { controller.dispatch(SpeakingFinished) }
        controller.orders.forEach(::execute)
    }

    private fun execute(order: RepetitionServiceOrder) {
        when (order) {
            is Speak -> speaker.speak(order.text, order.language)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        speaker.shutdown()
    }
}