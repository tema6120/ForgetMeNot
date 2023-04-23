package com.odnovolov.forgetmenot.presentation.screen.player.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity

class NotificationBuilder(private val context: Context) {
    var cardPosition: String? = null
    var contextText: CharSequence? = null
    var isPlaying: Boolean = true
    var isCompleted: Boolean = false

    private val pendingIntentFlags: Int
        get() {
            return if (VERSION.SDK_INT >= VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        }

    fun build(): Notification {
        return NotificationCompat.Builder(context, PlayerService.CHANNEL_ID)
            .setSmallIcon(R.mipmap.fmn_launcher)
            .setContentTitle(contentTitle())
            .setContentText(contextText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent())
            .addAction(if (isPlaying) pauseAction() else resumeAction())
            .build()
    }

    private fun contentTitle(): String {
        val baseTitle = context.getString(R.string.player_notification_title)
        return cardPosition?.let { cardPosition: String ->
            "$baseTitle ($cardPosition)"
        } ?: baseTitle
    }

    fun update() {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun notify() = notificationManager.notify(PlayerService.NOTIFICATION_ID, build())

        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            val isNotificationVisible = notificationManager.activeNotifications
                .any { it.id == PlayerService.NOTIFICATION_ID }
            if (isNotificationVisible) {
                notify()
            }
        } else {
            notify()
        }
    }

    private fun contentIntent(): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return PendingIntent.getActivity(context, 0, notificationIntent, pendingIntentFlags)
    }

    private fun pauseAction(): NotificationCompat.Action {
        val pauseIntent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_PAUSE
        }
        val pausePendingIntent: PendingIntent =
            PendingIntent.getService(context, 0, pauseIntent, pendingIntentFlags)
        return NotificationCompat.Action.Builder(
            R.drawable.ic_pause_28,
            context.getString(R.string.pause_in_notification),
            pausePendingIntent
        ).build()
    }

    private fun resumeAction(): NotificationCompat.Action {
        val resumeIntent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_RESUME
        }
        val resumePendingIntent: PendingIntent =
            PendingIntent.getService(context, 0, resumeIntent, pendingIntentFlags)
        val actionTitle = context.getString(
            if (isCompleted)
                R.string.play_one_more_lap_in_notification else
                R.string.resume_in_notification
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_play_28,
            actionTitle,
            resumePendingIntent
        ).build()
    }
}