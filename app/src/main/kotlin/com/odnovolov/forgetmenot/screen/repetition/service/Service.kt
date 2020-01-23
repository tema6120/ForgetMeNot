package com.odnovolov.forgetmenot.screen.repetition.service

import android.content.Intent
import android.os.IBinder
import com.odnovolov.forgetmenot.common.Speaker
import com.odnovolov.forgetmenot.common.base.BaseService
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceEvent.*
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceOrder.Speak
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionServiceOrder.StopSpeaking

class RepetitionService : BaseService() {
    private val controller = RepetitionServiceController()
    private val serviceModel = RepetitionServiceModel()
    private lateinit var speaker: Speaker
    private lateinit var notificationBuilder: NotificationBuilder

    override fun onCreate() {
        speaker = Speaker(this)
        speaker.setOnSpeakingFinished { controller.dispatch(SpeakingFinished) }
        notificationBuilder = NotificationBuilder(context = this)
        observeServiceModel()
        controller.orders.forEach(::execute)
    }

    private fun observeServiceModel() {
        with(serviceModel) {
            question.observe { question ->
                notificationBuilder.contextText = question
                notificationBuilder.update()
            }
            isPlaying.observe { isPlaying ->
                notificationBuilder.isPlaying = isPlaying
                if (isPlaying) {
                    startForeground(NOTIFICATION_ID, notificationBuilder.build())
                } else {
                    notificationBuilder.update()
                    stopForeground(/*removeNotification = */false)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> controller.dispatch(PauseClicked)
            ACTION_RESUME -> controller.dispatch(ResumeClicked)
        }
        return START_NOT_STICKY
    }

    private fun execute(order: RepetitionServiceOrder) {
        when (order) {
            is Speak -> speaker.speak(order.text, order.language)
            StopSpeaking -> speaker.stop()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        speaker.shutdown()
    }

    companion object {
        const val NOTIFICATION_ID = 2900
        const val CHANNEL_ID = "8907"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
    }
}