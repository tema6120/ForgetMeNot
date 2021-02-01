package com.odnovolov.forgetmenot.presentation.screen.player.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.odnovolov.forgetmenot.presentation.common.base.BaseService
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceEvent.PauseNotificationActionClicked
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceEvent.ResumeNotificationActionClicked
import kotlinx.coroutines.launch

class PlayerService : BaseService() {
    init {
        PlayerDiScope.isServiceAlive = true
    }

    private lateinit var notificationBuilder: NotificationBuilder
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = NotificationBuilder(context = this)
        coroutineScope.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            observeServiceModel(diScope.serviceModel)
        }
    }

    private fun observeServiceModel(serviceModel: PlayerServiceModel) {
        with(serviceModel) {
            question.observe { question ->
                notificationBuilder.contextText = question
                notificationBuilder.update()
            }
            isPlaying.observe { isPlaying: Boolean ->
                notificationBuilder.isPlaying = isPlaying
                if (isPlaying) {
                    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                    wakeLock = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        javaClass.canonicalName
                    )
                    wakeLock!!.acquire()
                    startForeground(NOTIFICATION_ID, notificationBuilder.build())
                } else {
                    notificationBuilder.update()
                    stopForeground(/*removeNotification = */false)
                    wakeLock?.run { if (isHeld) release() }
                }
            }
            isCompleted.observe { isCompleted: Boolean ->
                notificationBuilder.isCompleted = isCompleted
                notificationBuilder.update()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        coroutineScope.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            val controller = diScope.serviceController
            when (intent?.action) {
                ACTION_PAUSE -> controller.dispatch(PauseNotificationActionClicked)
                ACTION_RESUME -> controller.dispatch(ResumeNotificationActionClicked)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        PlayerDiScope.isServiceAlive = false
        wakeLock?.run { if (isHeld) release() }
    }

    companion object {
        const val NOTIFICATION_ID = 2900
        const val CHANNEL_ID = "8907"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
    }
}