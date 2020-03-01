package com.odnovolov.forgetmenot.presentation.screen.repetition.service

import android.content.Intent
import android.os.IBinder
import com.odnovolov.forgetmenot.common.base.BaseService
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionScopeCloser
import org.koin.android.ext.android.getKoin

class RepetitionService : BaseService() {
    private val koinScope = getKoin().getOrCreateScope<Repetition>(REPETITION_SCOPE_ID)
    private val serviceModel: RepetitionServiceModel by koinScope.inject()
    private val controller: RepetitionServiceController by koinScope.inject()
    private lateinit var notificationBuilder: NotificationBuilder

    override fun onCreate() {
        koinScope.get<RepetitionScopeCloser>().isServiceAlive = true
        notificationBuilder = NotificationBuilder(context = this)
        observeServiceModel()
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
            ACTION_PAUSE -> controller.onPauseNotificationActionClicked()
            ACTION_RESUME -> controller.onResumeNotificationActionClicked()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        koinScope.get<RepetitionScopeCloser>().isServiceAlive = false
    }

    companion object {
        const val NOTIFICATION_ID = 2900
        const val CHANNEL_ID = "8907"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
    }
}