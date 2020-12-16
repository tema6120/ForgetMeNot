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
    var contextText: CharSequence? = null
    var isPlaying: Boolean = true

    fun build(): Notification {
        return NotificationCompat.Builder(context, PlayerService.CHANNEL_ID)
            .setSmallIcon(R.mipmap.fmn_launcher)
            .setContentTitle(context.getString(R.string.player_notification_title))
            .setContentText(contextText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent())
            .addAction(if (isPlaying) pauseAction() else resumeAction())
            .build()
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
        return PendingIntent.getActivity(context, 0, notificationIntent, 0)
    }

    private fun pauseAction(): NotificationCompat.Action {
        val pauseIntent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(context, 0, pauseIntent, 0)
        return NotificationCompat.Action.Builder(
            R.drawable.ic_pause_dark_24dp,
            context.getString(R.string.pause_in_notification),
            pausePendingIntent
        ).build()
    }

    private fun resumeAction(): NotificationCompat.Action {
        val intent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_RESUME
        }
        val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
        return NotificationCompat.Action.Builder(
            R.drawable.ic_play_arrow_dark_24dp,
            context.getString(R.string.resume_in_notification),
            pendingIntent
        ).build()
    }
}