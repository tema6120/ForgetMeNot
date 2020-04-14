package com.odnovolov.forgetmenot.presentation.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionService

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDiScope.init(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.repetition_notification_channel_name)
            val descriptionText = getString(R.string.repetition_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(RepetitionService.CHANNEL_ID, name, importance)
                .apply { description = descriptionText }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}