package com.odnovolov.forgetmenot.presentation.screen.player.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseService
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceController.Command.ShowCannotGetAudioFocusMessage
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceEvent.*
import kotlinx.coroutines.launch

class PlayerService : BaseService() {
    init {
        PlayerDiScope.isServiceAlive = true
    }

    private lateinit var notificationBuilder: NotificationBuilder
    private var wakeLock: PowerManager.WakeLock? = null
    private val cannotGetAudiofocusToast: Toast by lazy {
        Toast.makeText(
            applicationContext,
            R.string.error_message_cannot_get_audio_focus,
            Toast.LENGTH_SHORT
        )
    }

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = NotificationBuilder(context = this)
        coroutineScope.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            observeServiceModel(diScope.serviceModel)
            diScope.serviceController.commands.observe(::executeCommand)
        }
    }

    private fun executeCommand(command: PlayerServiceController.Command) {
        when (command) {
            ShowCannotGetAudioFocusMessage -> cannotGetAudiofocusToast.show()
        }
    }

    private fun observeServiceModel(serviceModel: PlayerServiceModel) {
        with(serviceModel) {
            cardPosition.observe { cardPosition: String? ->
                notificationBuilder.cardPosition = cardPosition
                notificationBuilder.update()
            }
            question.observe { question: String ->
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
        const val ACTION_PAUSE = "com.odnovolov.forgetmenot.ACTION_PAUSE"
        const val ACTION_RESUME = "com.odnovolov.forgetmenot.ACTION_RESUME"
    }
}