package com.odnovolov.forgetmenot.screen.repetition.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.MainActivity

class NotificationBuilder(private val context: Context) {
    var contextText: CharSequence? = null
    var isPlaying: Boolean = true

    fun build(): Notification {
        return NotificationCompat.Builder(context, RepetitionService.CHANNEL_ID)
            .setSmallIcon(R.mipmap.fmn_launcher)
            .setContentTitle(context.getString(R.string.repetition_notification_title))
            .setContentText(contextText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent())
            .addAction(if (isPlaying) pauseAction() else resumeAction())
            .build()
    }

    fun update() {
        val mNotificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(RepetitionService.NOTIFICATION_ID, build())
    }

    private fun contentIntent(): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return PendingIntent.getActivity(context, 0, notificationIntent, 0)
    }

    private fun pauseAction(): NotificationCompat.Action {
        val pauseIntent = Intent(context, RepetitionService::class.java).apply {
            action = RepetitionService.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(context, 0, pauseIntent, 0)
        return NotificationCompat.Action.Builder(
            R.drawable.ic_pause_dark_24dp,
            context.getString(R.string.pause_action_title_in_repetition_notification),
            pausePendingIntent
        ).build()
    }

    private fun resumeAction(): NotificationCompat.Action {
        val intent = Intent(context, RepetitionService::class.java).apply {
            action = RepetitionService.ACTION_RESUME
        }
        val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
        return NotificationCompat.Action.Builder(
            R.drawable.ic_play_arrow_dark_24dp,
            context.getString(R.string.resume_action_title_in_repetition_notification),
            pendingIntent
        ).build()
    }
}