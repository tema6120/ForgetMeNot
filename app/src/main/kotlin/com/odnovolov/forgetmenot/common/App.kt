package com.odnovolov.forgetmenot.common

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import com.odnovolov.forgetmenot.common.database.DatabaseLifecycleManager

class App : Application(), LifecycleObserver {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(DatabaseLifecycleManager)
    }
}