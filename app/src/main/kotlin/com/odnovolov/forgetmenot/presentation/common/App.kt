package com.odnovolov.forgetmenot.presentation.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.persistence.DatabaseInitializer
import com.odnovolov.forgetmenot.presentation.screen.decksettings.deckSettingsModule
import com.odnovolov.forgetmenot.presentation.screen.editcard.editCardModule
import com.odnovolov.forgetmenot.presentation.screen.exercise.exerciseModule
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.addDeckModule
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.deckSortingModule
import com.odnovolov.forgetmenot.presentation.screen.home.homeModule
import com.odnovolov.forgetmenot.presentation.screen.intervals.intervalsModule
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.modifyIntervalModule
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.pronunciationModule
import com.odnovolov.forgetmenot.presentation.screen.repetition.repetitionModule
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.walkingModeSettingsModule
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionService
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.repetitionSettingsModule
import lastAnswerFilterModule
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import repetitionLapsModule
import speakPlanModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        val navigator = getKoin().get<Navigator>()
        registerActivityLifecycleCallbacks(navigator)
        registerActivityLifecycleCallbacks(DatabaseInitializer)
        createNotificationChannel()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                appModule, homeModule, addDeckModule, deckSortingModule, exerciseModule,
                editCardModule, deckSettingsModule, intervalsModule, modifyIntervalModule,
                pronunciationModule, speakPlanModule,  walkingModeSettingsModule,
                repetitionSettingsModule, lastAnswerFilterModule, repetitionLapsModule,
                repetitionModule
            )
        }
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